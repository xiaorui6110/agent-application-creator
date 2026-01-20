package com.xiaorui.agentapplicationcreator.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.agent.orchestrator.DefaultAgentOrchestrator;
import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.mapper.AgentTaskMapper;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * agent执行任务表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class AgentTaskServiceImpl extends ServiceImpl<AgentTaskMapper, AgentTask>  implements AgentTaskService{

    private static final int MAX_RETRY = 3;

    /**
     * 任务状态索引前缀
     */
    private static final String TASK_STATUS_INDEX_PREFIX = "task_status:";

    @Resource
    private RedisTemplate<String, AgentTask> agentTaskRedisTemplate;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

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
        state.setTaskStatus(AgentTaskStatusEnum.QUEUED);
        state.setCreateTime(LocalDateTime.now());
        state.setUpdateTime(LocalDateTime.now());
        agentTaskRedisTemplate.opsForValue().set(taskId, state, 24, TimeUnit.HOURS);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务 ID
     * @param status 任务状态
     */
    @Override
    public void updateStatus(String taskId, AgentTaskStatusEnum status) {
        AgentTask state = agentTaskRedisTemplate.opsForValue().get(taskId);
        state.setTaskStatus(status);
        state.setUpdateTime(LocalDateTime.now());
        agentTaskRedisTemplate.opsForValue().set(taskId, state);
    }

    /**
     * 保存最终输出
     *
     * @param taskId 任务 ID
     * @param output Agent 最终输出
     */
    @Override
    public void saveFinalOutput(String taskId, SystemOutput output) {
        AgentTask state = agentTaskRedisTemplate.opsForValue().get(taskId);
        state.setTaskStatus(AgentTaskStatusEnum.SUCCEEDED);
        // 只保存 Agent 回复
        state.setTaskResult(output.getAgentResponse());
        state.setUpdateTime(LocalDateTime.now());
        agentTaskRedisTemplate.opsForValue().set(taskId, state);
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
        AgentTask state = this.getById(taskId);
        AgentFailTypeEnum failType = classify(error);
        state.setFailType(failType);
        // 可重试
        if (failType == AgentFailTypeEnum.SYSTEM_RETRYABLE && state.getRetryCount() < MAX_RETRY) {

            state.setTaskStatus(AgentTaskStatusEnum.RETRY_WAITING);
            state.setRetryCount(state.getRetryCount() + 1);
            // 加上重试间隔
            state.setNextRetryTime(LocalDateTime.now().plusSeconds(backoff(state.getRetryCount())));

            // 用 String 类型的 RedisTemplate 维护反向索引（Set结构，key=task_status:system_retryable，value=taskId）
            String statusIndexKey = TASK_STATUS_INDEX_PREFIX + AgentFailTypeEnum.SYSTEM_RETRYABLE.getValue();
            stringRedisTemplate.opsForSet().add(statusIndexKey, taskId);
            agentTaskRedisTemplate.expire(statusIndexKey, 24, TimeUnit.HOURS);

        } else {
            state.setTaskStatus(AgentTaskStatusEnum.FAILED);
            state.setTaskError(error.getMessage());
            persistAsync(state);
        }
        save(state);
    }

    /**
     * 前端通过任务ID轮询获取任务执行结果
     *
     * @param taskId 任务 ID
     * @return 任务执行结果
     */
    @Override
    public SystemOutput getTask(String taskId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        AgentTaskStatusEnum taskStatus = agentTaskRedisTemplate.opsForValue().get(taskId).getTaskStatus();
        AgentTask agentTask = this.getByTaskId(taskId);
        // 直接排除掉另外其他的状态
        if (taskStatus == AgentTaskStatusEnum.INIT ||taskStatus == AgentTaskStatusEnum.QUEUED
                || taskStatus == AgentTaskStatusEnum.RUNNING || taskStatus == AgentTaskStatusEnum.RETRY_WAITING
                || taskStatus == AgentTaskStatusEnum.FAILED) {
            return SystemOutput.builder()
                    .taskId(taskId)
                    .taskStatus(taskStatus)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        // 最后 SUCCEEDED，则返回完整信息
        return  SystemOutput.builder()
                .threadId(agentTask.getThreadId())
                .userId(userId)
                .appId(agentTask.getAppId())
                .taskId(agentTask.getTaskId())
                .taskStatus(agentTask.getTaskStatus())
                .agentResponse(agentTask.getTaskResult())
                .fromMemory(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 通过任务ID获取任务（这个单纯就是类似于 getById 方法，内部调用）
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @Override
    public AgentTask getByTaskId(String taskId) {
        try {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("task_id", taskId);
            return getOne(queryWrapper);
        } catch (Exception e) {
            // 捕获所有查询异常，查询不到数据时，直接返回 null
            return null;
        }
    }

    /**
     * 异步落 MySQL
     *
     * @param state 任务状态
     */
    @Async("agentPersistExecutor")
    @Override
    public void persistAsync(AgentTask state) {
        try {
            this.saveOrUpdate(state);
        } catch (Exception e) {
            // 只能记日志，绝不能影响主流程
            log.error("Persist agent task failed, taskId={}", state.getTaskId(), e);
        }
    }

    /**
     * 手动重试任务
     *
     * @param taskId 任务ID
     */
    @Override
    public void manualRetryTask(String taskId) {

        String redisKey = "retry:task:" + taskId;
        AgentTask task = agentTaskRedisTemplate.opsForValue().get(redisKey);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }
        if (task.getTaskStatus() == AgentTaskStatusEnum.FAILED ||
                task.getTaskStatus() == AgentTaskStatusEnum.RETRY_WAITING) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败，任务状态失败或重试中");
        }
        // 幂等锁，防止重复点击
        Boolean locked = agentTaskRedisTemplate.opsForValue()
                .setIfAbsent("agent:retry:lock:" + taskId, task, 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败，请稍后重试");
        }
        // 创建实例并调用外部方法
        DefaultAgentOrchestrator defaultAgentOrchestrator = new DefaultAgentOrchestrator();
        defaultAgentOrchestrator.manualRetry(task);
    }

    /**
     * 分类错误类型
     */
    private AgentFailTypeEnum classify(Throwable e) {
        if (e instanceof TimeoutException
                || e instanceof IOException) {
            return AgentFailTypeEnum.SYSTEM_RETRYABLE;
        }
        return AgentFailTypeEnum.BUSINESS_FATAL;
    }

    /**
     * 计算重试间隔（简单退避）
     */
    private long backoff(int retryCount) {
        // 1s, 2s, 4s, 8s, max 30s
        return Math.min(1000L * (1L << retryCount), 30_000L);
    }


}
