package com.xiaorui.agentapplicationcreator.ai.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description: 智能体回复（经过 AI 优化后）
 * @author: xiaorui
 * @date: 2025-12-12 15:00
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    /* ================= 基础标识 ================= */

    /**
     * 本次对话线程 ID（强绑定 userId）
     */
    private String threadId;

    /**
     * 当前用户 ID
     */
    private String userId;

    /**
     * 本次 Agent 回复的唯一 ID（用于追踪、回放、排错）
     */
    private String messageId;

    /**
     * Agent 名称 / 标识
     */
    private String agentName;

    /* ================= 核心内容 ================= */

    /**
     * Agent 最终给用户的自然语言回复
     */
    private String reply;

    /**
     * 结构化回复（如果使用 JSON Schema / Structured Output）
     */
    private Object structuredReply;

    /**
     * 本次回复是否命中历史 / 缓存
     */
    private boolean fromMemory;

    /* ================= Agent 行为 ================= */

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

    /* ================= 时间与元数据 ================= */

    /**
     * 本次回复时间戳
     */
    private long timestamp;

    /**
     * 扩展元数据（调试 / 实验 / 灰度）
     */
    private Map<String, Object> metadata;
}
