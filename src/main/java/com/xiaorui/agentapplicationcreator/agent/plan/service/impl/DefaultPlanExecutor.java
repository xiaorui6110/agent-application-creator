package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

import com.xiaorui.agentapplicationcreator.agent.plan.manager.ExpectedChecker;
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
 * @description: 默认计划执行器
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
     * 执行操作（真正的文件操作）
     */
    private void executeOperation(ValidatedOperation op) throws IOException {

        switch (op.getOperationType()) {

            /* ========== 文件写入类 ========== */

            case CREATE_FILE -> {
                Files.createDirectories(op.getResolvedPath().getParent());
                Files.writeString(
                        op.getResolvedPath(),
                        op.getContent(),
                        StandardOpenOption.CREATE_NEW
                );
            }

            case OVERWRITE_FILE -> {
                Files.writeString(
                        op.getResolvedPath(),
                        op.getContent(),
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            }

            case APPEND_FILE -> {
                Files.writeString(
                        op.getResolvedPath(),
                        op.getContent(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            }

            /* ========== 文件读取 / 查询类 ========== */

            case READ_FILE -> {
                // Executor 只执行，不返回内容
                Files.readString(op.getResolvedPath());
            }

            case EXISTS -> {
                Files.exists(op.getResolvedPath());
            }

            /* ========== 文件结构操作 ========== */

            case DELETE_FILE -> {
                Files.deleteIfExists(op.getResolvedPath());
            }

            case MOVE_FILE, RENAME_FILE -> {
                Files.createDirectories(op.getResolvedTargetPath().getParent());
                Files.move(
                        op.getResolvedPath(),
                        op.getResolvedTargetPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            /* ========== 目录操作 ========== */

            case CREATE_DIRECTORY -> {
                Files.createDirectories(op.getResolvedPath());
            }

            case DELETE_EMPTY_DIRECTORY -> {
                try (DirectoryStream<Path> stream =
                             Files.newDirectoryStream(op.getResolvedPath())) {
                    if (stream.iterator().hasNext()) {
                        throw new IllegalStateException("目录非空，禁止删除");
                    }
                }
                Files.delete(op.getResolvedPath());
            }

            default -> throw new IllegalArgumentException("不支持的操作类型：" + op.getOperationType());
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
