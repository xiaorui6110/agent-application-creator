package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import org.springframework.scheduling.annotation.Async;

/**
 * @author xiaorui
 */
public interface AgentTaskService extends IService<AgentTask> {

    void initTask(String taskId, String threadId, String appId, String originalMessage);

    void updateStatus(String taskId, AgentTaskStatusEnum status);

    void saveFinalOutput(String taskId, SystemOutput output);

    void markFailed(String taskId, Throwable error);

    SystemOutput getTask(String taskId);

    AgentTask getByTaskId(String taskId);

    @Async("agentPersistExecutor")
    void persistAsync(AgentTask state);

    void manualRetryTask(String taskId);

    /**
     * Get the original user message used to create the task.
     *
     * @param taskId task id
     * @return original user message
     */
    String getOriginalMessage(String taskId);
}
