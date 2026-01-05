package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

import com.xiaorui.agentapplicationcreator.agent.plan.checker.ExpectedChecker;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedOperation;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.VerificationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.result.ExecutionResult;
import com.xiaorui.agentapplicationcreator.agent.plan.result.OperationResult;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * @description:
 * @author: xiaorui
 * @date: 2026-01-05 21:11
 **/
@Slf4j
@Service
public class DefaultPlanExecutor implements PlanExecutor {

    /**
     * 执行计划
     * @param plan 计划
     * @return 执行结果
     */
    @Override
    public ExecutionResult execute(ValidatedPlan plan) {

        List<OperationResult> results = new ArrayList<>();
        try {
            for (ValidatedOperation op : plan.getOperations()) {
                // 执行前校验 expected
                ExpectedChecker.check(op.getResolvedPath(), op.getExpected());
                // 执行操作
                executeOperation(op);
                results.add(new OperationResult(op.getOperationType(), op.getResolvedPath(), true, "执行成功"));
            }
            // 最终验证
            boolean verified = verify(plan.getVerification());
            return new ExecutionResult(true, results, verified, null);

        } catch (Exception e) {
            return new ExecutionResult(false, results, false, e.getMessage()
            );
        }
    }

    /**
     * 执行操作（真正的文件操作） TODO 后续肯定需要增加操作类型
     */
    private void executeOperation(ValidatedOperation op) throws IOException {

        switch (op.getOperationType()) {
            // 创建文件或覆盖文件
            case CREATE_FILE, OVERWRITE_FILE -> {
                Files.createDirectories(op.getResolvedPath().getParent());
                Files.writeString(
                        op.getResolvedPath(),
                        op.getContent(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            }
            // 删除文件
            case DELETE_FILE -> {
                Files.deleteIfExists(op.getResolvedPath());
            }
            // 移动文件
            case MOVE_FILE -> {
                Files.createDirectories(op.getResolvedTargetPath().getParent());
                Files.move(
                        op.getResolvedPath(),
                        op.getResolvedTargetPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            default -> throw new IllegalArgumentException("不支持的操作类型");
        }
    }

    /**
     * 验证 plan
     */
    private boolean verify(VerificationPlan verification) {
        // 没有声明验证，默认通过
        if (verification == null) {
            return true;
        }
        try {
            Path path = Paths.get(CODE_OUTPUT_ROOT_DIR).resolve(verification.getPath()).normalize();
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
