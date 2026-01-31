package com.xiaorui.agentapplicationcreator.agent.model.schema;

import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "对话线程 ID")
    private String threadId;

    /**
     * 当前用户 ID
     */
    @Schema(description = "当前用户 ID")
    private String userId;

    /**
     * 应用 ID
     */
    @Schema(description = "应用 ID")
    private String appId;

    /**
     * 任务 ID
     */
    @Schema(description = "任务 ID")
    private String taskId;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态")
    private String taskStatus;

    /**
     * Agent 名称 / 标识
     */
    @Schema(description = "Agent 名称 / 标识")
    private String agentName;

    /**
     * Agent 结构化回复
     */
    @Schema(description = "Agent 结构化回复")
    private AgentResponse agentResponse;

    /**
     * 本次回复是否命中历史 / 缓存（这个字段应该是无用的，因为 agent 的记忆就在 Redis 中，但是不删除了吧）
     */
    @Schema(description = "本次回复是否命中历史 / 缓存")
    private boolean fromMemory;

    /**
     * 本次回复时间戳（秒）
     */
    @Schema(description = "本次回复时间戳（秒）")
    private Long timestamp;

}

