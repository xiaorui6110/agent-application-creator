package com.xiaorui.agentapplicationcreator.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: Agent 任务状态
 * @author: xiaorui
 * @date: 2026-01-20 22:17
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1112379877092211932L;

    /**
     * 任务id
     */
    @Schema(description = "任务id")
    private String taskId;

    /**
     * 对话线程id
     */
    @Schema(description = "对话线程id")
    private String threadId;

    /**
     * 应用id
     */
    @Schema(description = "应用id")
    private String appId;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态")
    private String taskStatus;

    /**
     * 给用户看的信息
     */
    @Schema(description = "给用户看的信息")
    private String message;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 失败类型
     */
    @Schema(description = "失败类型")
    private String failType;

    /**
     * 下次重试时间
     */
    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
