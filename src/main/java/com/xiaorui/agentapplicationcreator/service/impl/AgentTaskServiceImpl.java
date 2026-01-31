package com.xiaorui.agentapplicationcreator.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.agent.orchestrator.DefaultAgentOrchestrator;
import com.xiaorui.agentapplicationcreator.constant.AgentTaskConstant;
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
        state.setTaskStatus(AgentTaskStatusEnum.QUEUED.getValue());
        state.setCreateTime(LocalDateTime.now());
        state.setUpdateTime(LocalDateTime.now());
        String keyName = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        agentTaskRedisTemplate.opsForValue().set(keyName, state, 24, TimeUnit.HOURS);
        // 异步落 MySQL
        persistAsync(state);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务 ID
     * @param status 任务状态
     */
    @Override
    public void updateStatus(String taskId, AgentTaskStatusEnum status) {
        String key = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        AgentTask state = agentTaskRedisTemplate.opsForValue().get(key);
        state.setTaskStatus(status.getValue());
        state.setUpdateTime(LocalDateTime.now());
        String keyName = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        agentTaskRedisTemplate.opsForValue().set(keyName, state);
        // 异步落 MySQL
        persistAsync(state);
    }

    /**
     * 保存最终输出
     *
     * @param taskId 任务 ID
     * @param output Agent 最终输出
     */
    @Override
    public void saveFinalOutput(String taskId, SystemOutput output) {
        String key = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        AgentTask state = agentTaskRedisTemplate.opsForValue().get(key);
        state.setTaskStatus(AgentTaskStatusEnum.SUCCEEDED.getValue());
        // 只保存 Agent 回复
        state.setTaskResult(output.getAgentResponse());
        state.setUpdateTime(LocalDateTime.now());
        String keyName = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        agentTaskRedisTemplate.opsForValue().set(keyName, state);
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
        AgentTask state = this.getByTaskId(taskId);
        if (state == null) {
            log.error("Task not found, taskId={}", taskId);
            return;
        }
        
        AgentFailTypeEnum failType = classify(error);
        state.setFailType(failType.getValue());
        // 可重试
        if (failType == AgentFailTypeEnum.SYSTEM_RETRYABLE && state.getRetryCount() < MAX_RETRY) {

            state.setTaskStatus(AgentTaskStatusEnum.RETRY_WAITING.getValue());
            state.setRetryCount(state.getRetryCount() + 1);
            // 加上重试间隔
            state.setNextRetryTime(LocalDateTime.now().plusSeconds(backoff(state.getRetryCount())));

            // 用 String 类型的 RedisTemplate 维护反向索引（Set结构，key=task_status:system_retryable，value=taskId）
            String statusIndexKey = AgentTaskConstant.TASK_STATUS_INDEX_PREFIX + AgentFailTypeEnum.SYSTEM_RETRYABLE.getValue();
            stringRedisTemplate.opsForSet().add(statusIndexKey, taskId);
            stringRedisTemplate.expire(statusIndexKey, 24, TimeUnit.HOURS);
            
            // 使用 saveOrUpdate 而不是 save，避免主键冲突
            this.saveOrUpdate(state);

        } else {
            state.setTaskStatus(AgentTaskStatusEnum.FAILED.getValue());
            state.setTaskError(error.getMessage());
            // 使用 saveOrUpdate 而不是 save，避免主键冲突
            this.saveOrUpdate(state);
        }
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
        String key = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        String taskStatus = agentTaskRedisTemplate.opsForValue().get(key).getTaskStatus();
        AgentTask agentTask = this.getByTaskId(taskId);
        // 直接排除掉另外其他的状态，if / else 简化之类的无所谓了
        if (taskStatus.equals(AgentTaskStatusEnum.INIT.getValue()) || taskStatus.equals(AgentTaskStatusEnum.QUEUED.getValue())
                || taskStatus.equals(AgentTaskStatusEnum.RUNNING.getValue()) || taskStatus.equals(AgentTaskStatusEnum.RETRY_WAITING.getValue())
                || taskStatus.equals(AgentTaskStatusEnum.FAILED.getValue())) {
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
        // 入参校验：避免无效数据库查询，直接返回 null
        if (taskId == null || taskId.trim().isEmpty()) {
            return null;
        }
        try {
            return QueryChain.of(AgentTask.class)
                    .eq(AgentTask::getTaskId, taskId)
                    .limit(1)
                    // MyBatis-Flex 查单个结果用 one()，无数据返回 null
                    .one();
        } catch (Exception e) {
            log.error("通过任务ID查询任务失败，taskId：{}", taskId, e);
            // 捕获所有查询异常，查询不到数据时，直接返回 null
            return null;
        }
    }

    /**
     * 异步落 MySQL
     *
     * @param agentTask 任务状态
     */
    @Async("agentPersistExecutor")
    @Override
    public void persistAsync(AgentTask agentTask) {

        String taskId = agentTask != null ? agentTask.getTaskId() : null;
        try {
            // 根据 taskId 判断数据是否存在
            AgentTask existingTask = QueryChain.of(AgentTask.class)
                    .eq(AgentTask::getTaskId, taskId)
                    .one();

            // 不存在则插入，存在则更新
            if (existingTask == null) {
                this.save(agentTask);

            } else {
                // 如果存在，需要设置 agentTaskId，否则 saveOrUpdate 会尝试插入而不是更新
                agentTask.setAgentTaskId(existingTask.getAgentTaskId());
                this.saveOrUpdate(agentTask);
            }
        } catch (Exception e) {
            if (agentTask != null) {
                log.error("Persist agent task failed, taskId={}", agentTask.getTaskId(), e);
            }
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

        if (task.getTaskStatus().equals(AgentTaskStatusEnum.FAILED.getValue())
                || task.getTaskStatus().equals(AgentTaskStatusEnum.RETRY_WAITING.getValue())) {
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
