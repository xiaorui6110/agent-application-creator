package com.xiaorui.agentapplicationcreator.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatus;
import com.xiaorui.agentapplicationcreator.mapper.AgentTaskMapper;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * agent执行任务表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class AgentTaskServiceImpl extends ServiceImpl<AgentTaskMapper, AgentTask>  implements AgentTaskService{

    @Resource
    private RedisTemplate<String, AgentTask> redisTemplate;

    /**
     * 初始化任务状态
     *
     * @param taskId 任务 ID
     * @param threadId 对话线程 ID
     * @param appId 应用 ID
     */
    @Override
    public void initTask(String taskId, String threadId, String appId) {
        AgentTask state = new AgentTask();
        state.setTaskId(taskId);
        state.setThreadId(threadId);
        state.setAppId(appId);
        state.setTaskStatus(AgentTaskStatus.QUEUED);
        state.setCreateTime(LocalDateTime.now());
        state.setUpdateTime(state.getCreateTime());
        redisTemplate.opsForValue().set(taskId, state, 24, TimeUnit.HOURS);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务 ID
     * @param status 任务状态
     */
    @Override
    public void updateStatus(String taskId, AgentTaskStatus status) {
        AgentTask state = redisTemplate.opsForValue().get(taskId);
        state.setTaskStatus(status);
        state.setUpdateTime(LocalDateTime.now());
        redisTemplate.opsForValue().set(taskId, state);
    }

    /**
     * 保存最终输出
     *
     * @param taskId 任务 ID
     * @param output 最终输出
     */
    @Override
    public void saveFinalOutput(String taskId, SystemOutput output) {
        AgentTask state = redisTemplate.opsForValue().get(taskId);
        state.setTaskStatus(AgentTaskStatus.SUCCEEDED);
        state.setTaskResult(output.toString());
        state.setUpdateTime(LocalDateTime.now());
        redisTemplate.opsForValue().set(taskId, state);
        // 异步落 MySQL
        persistAsync(state);
    }

    /**
     * 标记任务失败
     *
     * @param taskId 任务 ID
     * @param error 错误
     */
    @Override
    public void markFailed(String taskId, Throwable error) {
        AgentTask state = redisTemplate.opsForValue().get(taskId);
        state.setTaskStatus(AgentTaskStatus.FAILED);
        state.setTaskError(error.getMessage());
        state.setUpdateTime(LocalDateTime.now());
        redisTemplate.opsForValue().set(taskId, state);
        persistAsync(state);
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务 ID
     * @return 任务状态
     */
    @Override
    public AgentTaskStatus getTaskState(String taskId) {
        return redisTemplate.opsForValue().get(taskId).getTaskStatus();
    }

    /**
     * 异步落 MySQL
     *
     * @param state 任务状态
     */
    @Async("agentPersistExecutor")
    public void persistAsync(AgentTask state) {
        try {
            this.saveOrUpdate(state);
        } catch (Exception e) {
            // 只能记日志，绝不能影响主流程
            log.error("Persist agent task failed, taskId={}", state.getTaskId(), e);
        }
    }

}
