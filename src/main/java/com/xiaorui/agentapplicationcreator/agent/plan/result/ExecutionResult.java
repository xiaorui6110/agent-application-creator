package com.xiaorui.agentapplicationcreator.agent.plan.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @description: 执行结果
 * @author: xiaorui
 * @date: 2026-01-05 21:07
 **/
@Data
@AllArgsConstructor
public class ExecutionResult {

    /**
     * 是否全部执行成功
     */
    @Schema(description = "是否全部执行成功")
    private boolean success;

    /**
     * 每个操作的执行结果
     */
    @Schema(description = "每个操作的执行结果")
    private List<OperationResult> operationResults;

    /**
     * 是否通过最终验证
     */
    @Schema(description = "是否通过最终验证")
    private boolean verified;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 失败操作序号（从 0 开始）
     */
    @Schema(description = "失败操作序号")
    private Integer failedOperationIndex;

    /**
     * 是否已完成回滚
     */
    @Schema(description = "是否已完成回滚")
    private boolean rolledBack;
}

