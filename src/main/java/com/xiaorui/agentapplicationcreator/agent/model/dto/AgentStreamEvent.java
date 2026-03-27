package com.xiaorui.agentapplicationcreator.agent.model.dto;

import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStreamEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -2643858345980560646L;

    @Schema(description = "事件类型")
    private String event;

    @Schema(description = "任务 ID")
    private String taskId;

    @Schema(description = "会话线程 ID")
    private String threadId;

    @Schema(description = "应用 ID")
    private String appId;

    @Schema(description = "任务状态")
    private String taskStatus;

    @Schema(description = "阶段说明")
    private String message;

    @Schema(description = "Agent 名称")
    private String agentName;

    @Schema(description = "是否为结束事件")
    private Boolean done;

    @Schema(description = "事件时间戳")
    private Long timestamp;

    @Schema(description = "最终结构化结果，仅 done 事件返回")
    private SystemOutput result;
}
