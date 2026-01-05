package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.*;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * @description: 默认计划验证器
 * @author: xiaorui
 * @date: 2026-01-05 20:56
 **/
@Slf4j
@Service
public class DefaultPlanValidator implements PlanValidator {

    private static final int MAX_OPERATIONS = 5;

    private static final String DEFAULT_PLAN_TYPE = "CODE_MODIFICATION";

    private static final String DEFAULT_ROOT_DIR = "code_output";

    /**
     * 文件修改根目录
     */
    private static final Path ROOT_DIR = Paths.get(CODE_OUTPUT_ROOT_DIR).toAbsolutePath().normalize();

    /**
     * 验证计划
     * @param plan 计划
     * @return 验证结果
     */
    @Override
    public ValidatedPlan validate(CodeModificationPlan plan) {
        // 参数校验
        if (plan == null) {
            throw new IllegalArgumentException("Plan 不能为空");
        }
        if (!DEFAULT_PLAN_TYPE.equals(plan.getPlanType())) {
            throw new IllegalArgumentException("非法 planType");
        }
        if (!DEFAULT_ROOT_DIR.equals(plan.getRootDir())) {
            throw new IllegalArgumentException("rootDir 只能是 code_output");
        }
        List<FileOperationPlan> ops = plan.getOperations();
        if (ops == null || ops.isEmpty()) {
            throw new IllegalArgumentException("operations 不能为空");
        }
        if (ops.size() > MAX_OPERATIONS) {
            throw new IllegalArgumentException("操作数量超过限制：" + MAX_OPERATIONS);
        }

        List<ValidatedOperation> validatedOps = new ArrayList<>();
        for (FileOperationPlan op : ops) {
            validatedOps.add(validateOperation(op));
        }
        return new ValidatedPlan(ROOT_DIR, validatedOps, plan.getVerification()
        );
    }

    private ValidatedOperation validateOperation(FileOperationPlan op) {

        if (op.getOperationType() == null) {
            throw new IllegalArgumentException("op 不能为空");
        }
        if (op.getPath() == null || op.getPath().isBlank()) {
            throw new IllegalArgumentException("path 不能为空");
        }
        Path resolvedPath = resolveSafePath(op.getPath());
        ExpectedCondition expected = op.getExpected();
        if (expected == null) {
            throw new IllegalArgumentException("expected 条件是必须的");
        }
        validateExpected(expected);

        Path resolvedTarget = null;

        switch (op.getOperationType()) {
            case CREATE_FILE, OVERWRITE_FILE -> {
                if (op.getContent() == null) {
                    throw new IllegalArgumentException("写入类操作必须包含 content");
                }
            }
            case MOVE_FILE -> {
                if (op.getTargetPath() == null) {
                    throw new IllegalArgumentException("MOVE_FILE 必须包含 targetPath");
                }
                resolvedTarget = resolveSafePath(op.getTargetPath());
            }
            case DELETE_FILE -> {
                // 无额外要求
            }
            default -> throw new IllegalArgumentException("不支持的操作类型：" + op.getOperationType());
        }

        return new ValidatedOperation(op.getOperationType(), resolvedPath, resolvedTarget, expected, op.getContent()
        );
    }

    /**
     * 校验预期条件
     */
    private void validateExpected(ExpectedCondition expected) {

        switch (expected.getType()) {
            case CONTENT_EQUALS -> {
                if (expected.getValue() == null) {
                    throw new IllegalArgumentException("CONTENT_EQUALS 必须提供 value");
                }
            }
            case FILE_EXISTS, FILE_NOT_EXISTS -> {
                if (expected.getValue() != null) {
                    throw new IllegalArgumentException("该 expected 类型不应包含 value");
                }
            }
            default -> throw new IllegalArgumentException("未知 expected 类型");
        }
    }

    /**
     * 安全的路径解析
     */
    private Path resolveSafePath(String relativePath) {
        Path resolved = ROOT_DIR.resolve(relativePath).normalize();
        if (!resolved.startsWith(ROOT_DIR)) {
            throw new IllegalArgumentException("非法路径，越界访问：" + relativePath);
        }
        return resolved;
    }
}
