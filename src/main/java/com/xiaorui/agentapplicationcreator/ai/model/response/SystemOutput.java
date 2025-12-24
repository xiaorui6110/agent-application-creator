package com.xiaorui.agentapplicationcreator.ai.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 系统输出
 * @author: xiaorui
 * @date: 2025-12-24 14:59
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOutput {

    /**
     * 对话线程 ID（强绑定 userId）
     */
    private String threadId;

    /**
     * 当前用户 ID
     */
    private String userId;

    /**
     * 本次 Agent 回复的唯一 ID（用于追踪 / 回放 / 排错）
     */
    private String messageId;

    /**
     * Agent 名称 / 标识
     */
    private String agentName;

    /**
     * Agent 结构化回复
     */
    private AgentResponse agentResponse;

    /**
     * 本次回复是否命中历史 / 缓存
     */
    private boolean fromMemory;

    /**
     * 本次回复时间戳（毫秒）
     */
    private long timestamp;

}

