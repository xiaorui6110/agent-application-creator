package com.xiaorui.agentapplicationcreator.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1112379877092211932L;

    @Schema(description = "任务 ID")
    private String taskId;

    @Schema(description = "对话线程 ID")
    private String threadId;

    @Schema(description = "应用 ID")
    private String appId;

    @Schema(description = "任务状态")
    private String taskStatus;

    @Schema(description = "任务说明")
    private String message;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "失败类型")
    private String failType;

    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
