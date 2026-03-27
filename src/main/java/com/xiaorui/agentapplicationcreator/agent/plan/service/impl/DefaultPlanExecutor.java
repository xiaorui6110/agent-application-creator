package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.OperationSnapshot;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedOperation;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.VerificationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.manager.ExpectedChecker;
import com.xiaorui.agentapplicationcreator.agent.plan.result.ExecutionResult;
import com.xiaorui.agentapplicationcreator.agent.plan.result.OperationResult;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanExecutor;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaorui
 */
@Slf4j
@Service
public class DefaultPlanExecutor implements PlanExecutor {

    @Resource
    private AppProperties appProperties;

    @Override
    public ExecutionResult execute(ValidatedPlan plan) {
        if (plan.getOperations() == null || plan.getOperations().isEmpty()) {
            return new ExecutionResult(true, null, true, null, null, false);
        }
        List<OperationResult> results = new ArrayList<>();
        int currentIndex = -1;
        try {
            for (int i = 0; i < plan.getOperations().size(); i++) {
                currentIndex = i;
                ValidatedOperation op = plan.getOperations().get(i);
                ExpectedChecker.check(op.getResolvedPath(), op.getExpected());
                op.setSnapshot(captureSnapshot(op));
                executeOperation(op);
                results.add(new OperationResult(op.getOperationType(), op.getResolvedPath(), true, "success", false));
            }
            boolean verified = verify(plan.getVerification());
            if (!verified) {
                rollback(plan.getOperations(), currentIndex, results);
                return new ExecutionResult(false, results, false, "final verification failed", currentIndex, true);
            }
            return new ExecutionResult(true, results, true, null, null, false);
        } catch (Exception e) {
            rollback(plan.getOperations(), currentIndex, results);
            return new ExecutionResult(false, results, false, e.getMessage(), currentIndex, true);
        }
    }

    private void executeOperation(ValidatedOperation op) throws IOException {
        switch (op.getOperationType()) {
            case CREATE_FILE -> {
                Files.createDirectories(op.getResolvedPath().getParent());
                Files.writeString(op.getResolvedPath(), op.getContent(), StandardOpenOption.CREATE_NEW);
            }
            case OVERWRITE_FILE -> Files.writeString(
                    op.getResolvedPath(),
                    op.getContent(),
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            case APPEND_FILE -> Files.writeString(
                    op.getResolvedPath(),
                    op.getContent(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            case DELETE_FILE -> Files.deleteIfExists(op.getResolvedPath());
            case MOVE_FILE, RENAME_FILE -> {
                Files.createDirectories(op.getResolvedTargetPath().getParent());
                Files.move(op.getResolvedPath(), op.getResolvedTargetPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            case CREATE_DIRECTORY -> Files.createDirectories(op.getResolvedPath());
            case DELETE_EMPTY_DIRECTORY -> {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(op.getResolvedPath())) {
                    if (stream.iterator().hasNext()) {
                        throw new IllegalStateException("directory is not empty");
                    }
                }
                Files.delete(op.getResolvedPath());
            }
            default -> throw new IllegalArgumentException("unsupported operation type: " + op.getOperationType());
        }
    }

    private OperationSnapshot captureSnapshot(ValidatedOperation op) throws IOException {
        Path sourcePath = op.getResolvedPath();
        Path targetPath = op.getResolvedTargetPath();
        boolean sourceExisted = Files.exists(sourcePath);
        String sourceContent = readFileContentIfExists(sourcePath, sourceExisted);
        boolean targetExisted = targetPath != null && Files.exists(targetPath);
        String targetContent = readFileContentIfExists(targetPath, targetExisted);
        return new OperationSnapshot(sourceExisted, sourceContent, targetExisted, targetContent, sourcePath, targetPath);
    }

    private String readFileContentIfExists(Path path, boolean existed) throws IOException {
        if (!existed || path == null || Files.isDirectory(path)) {
            return null;
        }
        return Files.readString(path);
    }

    private void rollback(List<ValidatedOperation> operations, int currentIndex, List<OperationResult> results) {
        if (currentIndex < 0) {
            return;
        }
        for (int i = currentIndex; i >= 0; i--) {
            ValidatedOperation operation = operations.get(i);
            try {
                rollbackOperation(operation);
                markRolledBack(results, operation, "rolled back");
            } catch (Exception rollbackError) {
                log.error("rollback failed, operationIndex={}, path={}", i, operation.getResolvedPath(), rollbackError);
                markRolledBack(results, operation, "rollback failed: " + rollbackError.getMessage());
            }
        }
    }

    private void rollbackOperation(ValidatedOperation op) throws IOException {
        if (op.getSnapshot() == null) {
            return;
        }
        OperationSnapshot snapshot = op.getSnapshot();
        switch (op.getOperationType()) {
            case CREATE_FILE -> deletePathIfExists(snapshot.getSourcePath());
            case OVERWRITE_FILE, APPEND_FILE -> restoreFile(snapshot.getSourcePath(), snapshot.isSourceExisted(), snapshot.getSourceContent());
            case DELETE_FILE -> restoreFile(snapshot.getSourcePath(), snapshot.isSourceExisted(), snapshot.getSourceContent());
            case MOVE_FILE, RENAME_FILE -> rollbackMove(snapshot);
            case CREATE_DIRECTORY -> deleteDirectoryIfEmpty(snapshot.getSourcePath(), snapshot.isSourceExisted());
            case DELETE_EMPTY_DIRECTORY -> {
                if (snapshot.isSourceExisted()) {
                    Files.createDirectories(snapshot.getSourcePath());
                }
            }
            default -> throw new IllegalArgumentException("unsupported rollback type: " + op.getOperationType());
        }
    }

    private void rollbackMove(OperationSnapshot snapshot) throws IOException {
        if (snapshot.getTargetPath() != null && Files.exists(snapshot.getTargetPath())) {
            Files.createDirectories(snapshot.getSourcePath().getParent());
            Files.move(snapshot.getTargetPath(), snapshot.getSourcePath(), StandardCopyOption.REPLACE_EXISTING);
        }
        restoreFile(snapshot.getTargetPath(), snapshot.isTargetExisted(), snapshot.getTargetContent());
        if (!snapshot.isSourceExisted()) {
            deletePathIfExists(snapshot.getSourcePath());
        }
    }

    private void restoreFile(Path path, boolean existed, String content) throws IOException {
        if (path == null) {
            return;
        }
        if (!existed) {
            deletePathIfExists(path);
            return;
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content == null ? "" : content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void deletePathIfExists(Path path) throws IOException {
        if (path == null) {
            return;
        }
        Files.deleteIfExists(path);
    }

    private void deleteDirectoryIfEmpty(Path path, boolean existedBefore) throws IOException {
        if (path == null || existedBefore || !Files.exists(path)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            if (!stream.iterator().hasNext()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private void markRolledBack(List<OperationResult> results, ValidatedOperation operation, String message) {
        for (int i = results.size() - 1; i >= 0; i--) {
            OperationResult result = results.get(i);
            if (result.getOperationType() == operation.getOperationType()
                    && result.getPath().equals(operation.getResolvedPath())) {
                result.setRolledBack(true);
                result.setMessage(message);
                return;
            }
        }
        results.add(new OperationResult(operation.getOperationType(), operation.getResolvedPath(), false, message, true));
    }

    private boolean verify(VerificationPlan verification) {
        if (verification == null) {
            return true;
        }
        try {
            Path path = appProperties.resolvePathWithinRoot(appProperties.getCodeOutputRootPath(), verification.getPath());
            if (verification.getType() == VerificationPlan.Type.CONTENT_EQUALS) {
                String actual = Files.readString(path);
                return actual.equals(verification.getValue());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
