package com.xiaorui.agentapplicationcreator.ai.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description: 智能体结构化输出回复（经过 GPT 优化后）
 * @author: xiaorui
 * @date: 2025-12-12 15:00
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    /**
     * Agent 最终给用户的自然语言回复
     * ⚠️ 严禁包含代码
     */
    private String reply;

    /**
     * 结构化回复（机器可消费）
     * ⚠️ 所有代码只能出现在这里
     */
    private StructuredReply structuredReply;

    /**
     * Agent 在本次对话中调用的工具信息
     */
    private List<ToolCallResponse> toolCalls;

    /**
     * Agent 对用户意图的理解摘要（非 Chain-of-Thought）
     */
    private String intentSummary;

    /**
     * Agent 自评置信度（0 ~ 1）
     */
    private Double confidence;

    /**
     * 扩展元数据（调试 / 灰度 / 实验）
     */
    private Map<String, Object> metadata;
}

