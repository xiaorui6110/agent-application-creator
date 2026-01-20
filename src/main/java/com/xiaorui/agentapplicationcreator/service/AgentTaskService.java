package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import org.springframework.scheduling.annotation.Async;

/**
 * agent执行任务表 服务层。
 *
 * @author xiaorui
 */
public interface AgentTaskService extends IService<AgentTask> {

    /**
     * 初始化任务状态
     *
     * @param taskId 任务 ID
     * @param threadId 对话线程 ID
     * @param appId 应用 ID
     */
    void initTask(String taskId, String threadId, String appId);

    /**
     * 更新任务状态
     *
     * @param taskId 任务 ID
     * @param status 任务状态
     */
    void updateStatus(String taskId, AgentTaskStatusEnum status);

    /**
     * 保存最终输出
     *
     * @param taskId 任务 ID
     * @param output 最终输出
     */
    void saveFinalOutput(String taskId, SystemOutput output);

    /**
     * 标记任务失败
     *
     * @param taskId 任务 ID
     * @param error 错误
     */
    void markFailed(String taskId, Throwable error);

    /**
     * 通过任务ID获取任务执行结果
     *
     * @param taskId 任务ID
     * @return 任务执行结果
     */
    SystemOutput getTask(String taskId);

    /**
     * 通过任务ID获取任务
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    AgentTask getByTaskId(String taskId);

    /**
     * 异步保存任务状态
     * @param state 任务状态
     */
    @Async("agentPersistExecutor")
    void persistAsync(AgentTask state);

    /**
     * 手动重试任务
     *
     * @param taskId 任务ID
     */
    void manualRetryTask(String taskId);

}
