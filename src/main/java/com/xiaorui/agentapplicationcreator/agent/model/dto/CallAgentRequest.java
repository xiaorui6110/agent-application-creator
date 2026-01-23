package com.xiaorui.agentapplicationcreator.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 智能体请求
 * @author: xiaorui
 * @date: 2025-12-12 15:00
 **/
@Data
public class CallAgentRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5172570689175769832L;

    /**
     * 消息
     */
    @NotBlank
    @Size(max = 2000)
    @Schema(description = "消息")
    private String message;

    /**
     * 线程 ID（threadId 是给定对话的唯一标识符，使用 threadId 维护对话上下文）
     * 前端可选传 threadId（继续对话）
     */
    @Schema(description = "线程id")
    private String threadId;

    /**
     * 应用 ID（应用级 agent 对话标识，用于维护应用中的对话）
     */
    @Schema(description = "应用id")
    private String appId;


}
