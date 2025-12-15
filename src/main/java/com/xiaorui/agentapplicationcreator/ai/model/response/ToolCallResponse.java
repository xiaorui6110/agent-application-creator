package com.xiaorui.agentapplicationcreator.ai.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 工具调用结果
 * @author: xiaorui
 * @date: 2025-12-15 15:48
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallResponse {

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具输入（序列化后的 JSON）
     */
    private Object input;

    /**
     * 工具输出（序列化后的 JSON）
     */
    private Object output;

    /**
     * 工具执行是否成功
     */
    private boolean success;

    /**
     * 执行耗时（ms）
     */
    private Long costMs;
}

