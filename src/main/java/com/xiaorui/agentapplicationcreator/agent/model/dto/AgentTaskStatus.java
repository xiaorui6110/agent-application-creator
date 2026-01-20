package com.xiaorui.agentapplicationcreator.agent.model.dto;

import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
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
    private String taskId;

    /**
     * 对话线程id
     */
    private String threadId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 任务状态
     */
    private AgentTaskStatusEnum taskStatus;

    /**
     * 给用户看的信息
     */
    private String message;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 失败类型
     */
    private AgentFailTypeEnum failType;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
