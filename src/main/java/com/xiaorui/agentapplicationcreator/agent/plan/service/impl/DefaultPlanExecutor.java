package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

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

@Slf4j
@Service
public class DefaultPlanExecutor implements PlanExecutor {

    @Resource
    private AppProperties appProperties;

    @Override
    public ExecutionResult execute(ValidatedPlan plan) {
        if (plan.getOperations() == null || plan.getOperations().isEmpty()) {
            return new ExecutionResult(true, null, true, null);
        }
        List<OperationResult> results = new ArrayList<>();
        try {
            for (ValidatedOperation op : plan.getOperations()) {
                ExpectedChecker.check(op.getResolvedPath(), op.getExpected());
                executeOperation(op);
                results.add(new OperationResult(op.getOperationType(), op.getResolvedPath(), true, "success"));
            }
            boolean verified = verify(plan.getVerification());
            return new ExecutionResult(true, results, verified, null);
        } catch (Exception e) {
            return new ExecutionResult(false, results, false, e.getMessage());
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
            case READ_FILE -> Files.readString(op.getResolvedPath());
            case EXISTS -> Files.exists(op.getResolvedPath());
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