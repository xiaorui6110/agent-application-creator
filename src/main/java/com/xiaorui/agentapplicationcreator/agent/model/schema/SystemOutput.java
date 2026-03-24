package com.xiaorui.agentapplicationcreator.agent.model.schema;

import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOutput {

    @Schema(description = "对话线程 ID")
    private String threadId;

    @Schema(description = "当前用户 ID")
    private String userId;

    @Schema(description = "应用 ID")
    private String appId;

    @Schema(description = "任务 ID")
    private String taskId;

    @Schema(description = "任务状态")
    private String taskStatus;

    @Schema(description = "任务状态说明")
    private String message;

    @Schema(description = "Agent 名称")
    private String agentName;

    @Schema(description = "Agent 结构化响应")
    private AgentResponse agentResponse;

    @Schema(description = "是否命中历史或缓存")
    private boolean fromMemory;

    @Schema(description = "失败类型")
    private String failType;

    @Schema(description = "失败原因")
    private String taskError;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "响应时间戳")
    private Long timestamp;
}
