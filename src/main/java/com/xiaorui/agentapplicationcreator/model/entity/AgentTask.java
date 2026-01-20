package com.xiaorui.agentapplicationcreator.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * agent执行任务表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_agent_task")
public class AgentTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * agent执行任务id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String agentTaskId;

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
     * 任务结果
     */
    private AgentResponse taskResult;

    /**
     * 任务错误信息
     */
    private String taskError;

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
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime nextRetryTime;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
