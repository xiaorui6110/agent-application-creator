package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.CodeModificationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ExpectedCondition;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.FileOperationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedOperation;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.enums.OperationTypeEnum;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanValidator;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultPlanValidator implements PlanValidator {

    private static final int MAX_OPERATIONS = 150;

    private static final String DEFAULT_PLAN_TYPE = "CODE_MODIFICATION";

    private static final String DEFAULT_ROOT_DIR = "code_output";

    private static final List<OperationTypeEnum> SUPPORTED_OPERATION_TYPES = List.of(
            OperationTypeEnum.CREATE_FILE,
            OperationTypeEnum.OVERWRITE_FILE,
            OperationTypeEnum.APPEND_FILE,
            OperationTypeEnum.DELETE_FILE,
            OperationTypeEnum.MOVE_FILE,
            OperationTypeEnum.RENAME_FILE,
            OperationTypeEnum.CREATE_DIRECTORY,
            OperationTypeEnum.DELETE_EMPTY_DIRECTORY
    );

    @Resource
    private AppProperties appProperties;

    @Override
    public ValidatedPlan validate(CodeModificationPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("plan is blank");
        }
        if (!DEFAULT_PLAN_TYPE.equals(plan.getPlanType())) {
            throw new IllegalArgumentException("illegal planType");
        }
        if (!DEFAULT_ROOT_DIR.equals(plan.getRootDir())) {
            throw new IllegalArgumentException("rootDir must be code_output");
        }
        List<FileOperationPlan> ops = plan.getOperations();
        if (ops == null || ops.isEmpty()) {
            return new ValidatedPlan(getRootDir(), null, plan.getVerification());
        }
        if (ops.size() > MAX_OPERATIONS) {
            throw new IllegalArgumentException("too many operations: " + MAX_OPERATIONS);
        }
        List<ValidatedOperation> validatedOps = new ArrayList<>();
        for (FileOperationPlan op : ops) {
            validatedOps.add(validateOperation(op));
        }
        return new ValidatedPlan(getRootDir(), validatedOps, plan.getVerification());
    }

    private ValidatedOperation validateOperation(FileOperationPlan op) {
        OperationTypeEnum type = op.getOperationType();
        if (type == null) {
            throw new IllegalArgumentException("operationType is blank");
        }
        if (!SUPPORTED_OPERATION_TYPES.contains(type)) {
            throw new IllegalArgumentException("unsupported operationType in main flow: " + type);
        }
        if (op.getPath() == null || op.getPath().isBlank()) {
            throw new IllegalArgumentException("path is blank");
        }

        Path resolvedPath = resolveSafePath(op.getPath());
        Path resolvedTarget = null;

        ExpectedCondition expected = op.getExpected();
        if (expected == null) {
            throw new IllegalArgumentException("expected is blank");
        }
        validateExpected(expected);

        switch (type) {
            case CREATE_FILE, OVERWRITE_FILE, APPEND_FILE -> {
                if (op.getContent() == null) {
                    throw new IllegalArgumentException(type + " requires content");
                }
            }
            case DELETE_FILE, CREATE_DIRECTORY, DELETE_EMPTY_DIRECTORY -> {
            }
            case MOVE_FILE, RENAME_FILE -> {
                if (op.getTargetPath() == null) {
                    throw new IllegalArgumentException(type + " requires targetPath");
                }
                resolvedTarget = resolveSafePath(op.getTargetPath());
            }
            default -> throw new IllegalArgumentException("unknown operationType: " + type);
        }

        return new ValidatedOperation(type, resolvedPath, resolvedTarget, expected, op.getContent(), null);
    }

    private void validateExpected(ExpectedCondition expected) {
        switch (expected.getType()) {
            case CONTENT_EQUALS -> {
                if (expected.getValue() == null) {
                    throw new IllegalArgumentException("CONTENT_EQUALS requires value");
                }
            }
            case FILE_EXISTS, FILE_NOT_EXISTS -> {
                if (expected.getValue() != null) {
                    throw new IllegalArgumentException("current expected type does not allow value");
                }
            }
            default -> throw new IllegalArgumentException("unknown expected type");
        }
    }

    private Path resolveSafePath(String relativePath) {
        return appProperties.resolvePathWithinRoot(getRootDir(), relativePath);
    }

    private Path getRootDir() {
        return appProperties.getCodeOutputRootPath();
    }
}
