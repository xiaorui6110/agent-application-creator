package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatus;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;

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
    void updateStatus(String taskId, AgentTaskStatus status);

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
     * 获取任务状态
     *
     * @param taskId 任务 ID
     * @return 任务状态
     */
    AgentTaskStatus getTaskState(String taskId);


}
