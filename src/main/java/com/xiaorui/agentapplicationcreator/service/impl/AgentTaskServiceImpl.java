package com.xiaorui.agentapplicationcreator.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.agent.orchestrator.DefaultAgentOrchestrator;
import com.xiaorui.agentapplicationcreator.constant.AgentTaskConstant;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.stream.AgentTaskStreamManager;
import com.xiaorui.agentapplicationcreator.mapper.AgentTaskMapper;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author xiaorui
 */
@Slf4j
@Service
public class AgentTaskServiceImpl extends ServiceImpl<AgentTaskMapper, AgentTask> implements AgentTaskService {

    private static final int MAX_RETRY = 3;

    @Resource
    private RedisTemplate<String, AgentTask> agentTaskRedisTemplate;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

    @Lazy
    @Resource
    private DefaultAgentOrchestrator defaultAgentOrchestrator;

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private AgentTaskStreamManager agentTaskStreamManager;

    @Override
    public void initTask(String taskId, String threadId, String appId) {
        AgentTask state = new AgentTask();
        state.setTaskId(taskId);
        state.setThreadId(threadId);
        state.setAppId(appId);
        state.setTaskStatus(AgentTaskStatusEnum.QUEUED.getValue());
        state.setRetryCount(0);
        state.setCreateTime(LocalDateTime.now());
        state.setUpdateTime(LocalDateTime.now());
        saveTaskState(state);
        persistAsync(state);
        agentTaskStreamManager.publishStatus(taskId, buildTaskOutputSnapshot(state, false));
    }

    @Override
    public void updateStatus(String taskId, AgentTaskStatusEnum status) {
        AgentTask state = getTaskStateOrThrow(taskId);
        state.setTaskStatus(status.getValue());
        state.setUpdateTime(LocalDateTime.now());
        saveTaskState(state);
        persistAsync(state);
        agentTaskStreamManager.publishStatus(taskId, buildTaskOutputSnapshot(state, false));
    }

    @Override
    public void saveFinalOutput(String taskId, SystemOutput output) {
        AgentTask state = getTaskStateOrThrow(taskId);
        state.setTaskStatus(AgentTaskStatusEnum.SUCCEEDED.getValue());
        state.setTaskResult(output.getAgentResponse());
        state.setTaskError(null);
        state.setFailType(null);
        state.setNextRetryTime(null);
        state.setUpdateTime(LocalDateTime.now());
        saveTaskState(state);
        persistAsync(state);
        agentTaskStreamManager.publishDone(taskId, buildTaskOutputSnapshot(state, true));
    }

    @Override
    public void markFailed(String taskId, Throwable error) {
        AgentTask state = getTaskState(taskId);
        if (state == null) {
            log.error("Task not found, taskId={}", taskId);
            return;
        }

        AgentFailTypeEnum failType = classify(error);
        state.setFailType(failType.getValue());
        state.setTaskError(error != null ? error.getMessage() : "unknown error");
        state.setUpdateTime(LocalDateTime.now());

        if (failType == AgentFailTypeEnum.SYSTEM_RETRYABLE && state.getRetryCount() < MAX_RETRY) {
            state.setTaskStatus(AgentTaskStatusEnum.RETRY_WAITING.getValue());
            state.setRetryCount(state.getRetryCount() + 1);
            state.setNextRetryTime(LocalDateTime.now().plusSeconds(backoffSeconds(state.getRetryCount())));
            String statusIndexKey = AgentTaskConstant.TASK_STATUS_INDEX_PREFIX + AgentFailTypeEnum.SYSTEM_RETRYABLE.getValue();
            stringRedisTemplate.opsForSet().add(statusIndexKey, taskId);
            stringRedisTemplate.expire(statusIndexKey, 24, TimeUnit.HOURS);
        } else {
            state.setTaskStatus(AgentTaskStatusEnum.FAILED.getValue());
            state.setNextRetryTime(null);
        }

        saveTaskState(state);
        persistAsync(state);
        agentTaskStreamManager.publishFailed(taskId, buildTaskOutputSnapshot(state, false));
    }

    @Override
    public SystemOutput getTask(String taskId) {
        AgentTask agentTask = getTaskState(taskId);
        ThrowUtil.throwIf(agentTask == null, ErrorCode.NOT_FOUND_ERROR, "task not found");
        validateTaskAccess(agentTask);

        SystemOutput snapshot = buildTaskOutputSnapshot(agentTask, true);
        snapshot.setUserId(SecurityUtil.getUserInfo().getUserId());
        return snapshot;
    }

    @Override
    public AgentTask getByTaskId(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return null;
        }
        try {
            return QueryChain.of(AgentTask.class)
                    .eq(AgentTask::getTaskId, taskId)
                    .limit(1)
                    .one();
        } catch (Exception e) {
            log.error("Query agent task failed, taskId={}", taskId, e);
            return null;
        }
    }

    @Async("agentPersistExecutor")
    @Override
    public void persistAsync(AgentTask agentTask) {
        String taskId = agentTask != null ? agentTask.getTaskId() : null;
        try {
            AgentTask existingTask = QueryChain.of(AgentTask.class)
                    .eq(AgentTask::getTaskId, taskId)
                    .one();
            if (existingTask == null) {
                this.save(agentTask);
            } else {
                agentTask.setAgentTaskId(existingTask.getAgentTaskId());
                this.saveOrUpdate(agentTask);
            }
        } catch (Exception e) {
            if (agentTask != null) {
                log.error("Persist agent task failed, taskId={}", agentTask.getTaskId(), e);
            }
        }
    }

    @Override
    public void manualRetryTask(String taskId) {
        AgentTask task = getTaskStateOrThrow(taskId);
        validateTaskAccess(task);
        if (!AgentTaskStatusEnum.FAILED.getValue().equals(task.getTaskStatus())
                && !AgentTaskStatusEnum.RETRY_WAITING.getValue().equals(task.getTaskStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "current task status does not support retry");
        }
        Boolean locked = agentTaskRedisTemplate.opsForValue()
                .setIfAbsent("agent:retry:lock:" + taskId, task, 30, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "please retry later");
        }
        defaultAgentOrchestrator.manualRetry(task);
    }

    private AgentFailTypeEnum classify(Throwable error) {
        if (error instanceof TimeoutException || error instanceof IOException) {
            return AgentFailTypeEnum.SYSTEM_RETRYABLE;
        }
        return AgentFailTypeEnum.BUSINESS_FATAL;
    }

    private long backoffSeconds(int retryCount) {
        return Math.min(1L << retryCount, 30L);
    }

    private AgentTask getTaskState(String taskId) {
        String key = AgentTaskConstant.AGENT_TASK_PREFIX + taskId;
        AgentTask state = agentTaskRedisTemplate.opsForValue().get(key);
        if (state != null) {
            return state;
        }
        return this.getByTaskId(taskId);
    }

    private AgentTask getTaskStateOrThrow(String taskId) {
        AgentTask state = getTaskState(taskId);
        if (state == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "task not found");
        }
        return state;
    }

    private void saveTaskState(AgentTask state) {
        String keyName = AgentTaskConstant.AGENT_TASK_PREFIX + state.getTaskId();
        agentTaskRedisTemplate.opsForValue().set(keyName, state, 24, TimeUnit.HOURS);
    }

    private void validateTaskAccess(AgentTask task) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "user not found");
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return;
        }
        ThrowUtil.throwIf(task.getAppId() == null, ErrorCode.NOT_FOUND_ERROR, "task app not found");
        App app = appService.getById(task.getAppId());
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "app not found");
        ThrowUtil.throwIf(!userId.equals(app.getUserId()), ErrorCode.NOT_AUTH_ERROR, "no permission for task");
    }

    private String buildTaskMessage(String normalizedStatus, AgentTask agentTask) {
        return switch (normalizedStatus) {
            case "WAITING" -> "task is waiting in queue";
            case "RUNNING" -> "task is running";
            case "RETRY_WAITING" -> "task failed and is waiting for retry";
            case "FAILED" -> agentTask.getTaskError() != null ? agentTask.getTaskError() : "task failed";
            case "SUCCEEDED" -> "task succeeded";
            default -> "unknown task status";
        };
    }

    private SystemOutput buildTaskOutputSnapshot(AgentTask agentTask, boolean includeResult) {
        String normalizedStatus = AgentTaskStatusEnum.toApiValue(agentTask.getTaskStatus());
        SystemOutput.SystemOutputBuilder builder = SystemOutput.builder()
                .threadId(agentTask.getThreadId())
                .appId(agentTask.getAppId())
                .taskId(agentTask.getTaskId())
                .taskStatus(normalizedStatus)
                .message(buildTaskMessage(normalizedStatus, agentTask))
                .agentName("app_creator_agent")
                .failType(agentTask.getFailType())
                .taskError(agentTask.getTaskError())
                .retryCount(agentTask.getRetryCount())
                .nextRetryTime(agentTask.getNextRetryTime())
                .fromMemory(false)
                .timestamp(System.currentTimeMillis());
        if (includeResult && "SUCCEEDED".equals(normalizedStatus)) {
            builder.agentResponse(agentTask.getTaskResult());
        }
        return builder.build();
    }
}
