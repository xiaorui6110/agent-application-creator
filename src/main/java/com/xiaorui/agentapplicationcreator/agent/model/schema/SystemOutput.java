package com.xiaorui.agentapplicationcreator.agent.model.schema;

import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 系统输出（主要信息是 "平台"）
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
     * 应用 ID
     */
    private String appId;

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
     * 本次回复时间戳（秒）
     */
    private long timestamp;

}

