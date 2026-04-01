package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.dto.AgentTaskStatus;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.constant.AgentTaskConstant;
import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.enums.AppVersionSourceEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.stream.AgentTaskStreamManager;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.AppVersionService;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.service.UserThreadBindService;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum.RUNNING;
import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum.SUCCEEDED;

/**
 * @author xiaorui
 */
@Component
@Slf4j
public class DefaultAgentOrchestrator implements AgentOrchestrator {

    private static final String TASK_STATUS_INDEX_PREFIX = "task_status:";
    private static final Integer MAX_INPUT_LENGTH = 2000;

    @Resource
    private AgentAppCreator agentAppCreator;

    @Resource
    private AgentTaskExecutor agentTaskExecutor;

    @Resource
    private AgentTaskService agentTaskService;

    @Resource
    private UserService userService;

    @Resource
    private UserThreadBindService userThreadBindService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private RedisTemplate<String, AgentTask> agentTaskRedisTemplate;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

    @Resource
    private CodeFileSaverUtil codeFileSaverUtil;

    @Resource
    private AppVersionService appVersionService;

    @Resource
    private AppService appService;

    @Resource
    private AgentTaskStreamManager agentTaskStreamManager;

    @Override
    public AgentTaskStatus handleUserMessage(String message, String threadId, String appId) {
        validateUserInput(message);

        String taskId = UUID.randomUUID().toString();
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "user not found");
        appService.validateAppAccess(appId);

        String updatedThreadId = Optional.ofNullable(threadId)
                .filter(StrUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());

        log.info("Agent task accepted, taskId={}, threadId={}, appId={}, userId={}",
                taskId, updatedThreadId, appId, userId);
        userThreadBindService.ensureThreadOwnership(userId, updatedThreadId, "app_creator_agent");
        agentTaskService.initTask(taskId, updatedThreadId, appId, message);
        chatHistoryService.saveChatHistory(appId, userId, message, "user");
        agentTaskStreamManager.publishProgress(taskId, updatedThreadId, appId, "WAITING",
                "task queued and waiting to start", "app_creator_agent");

        agentTaskExecutor.submitAgentTask(taskId, () -> executeMainTask(taskId, updatedThreadId, appId, userId, message));

        AgentTask agentTask = agentTaskService.getByTaskId(taskId);
        return AgentTaskStatus.builder()
                .taskId(taskId)
                .threadId(updatedThreadId)
                .appId(appId)
                .taskStatus(AgentTaskStatusEnum.toApiValue(agentTask.getTaskStatus()))
                .message("task queued")
                .retryCount(agentTask.getRetryCount())
                .failType(agentTask.getFailType())
                .nextRetryTime(agentTask.getNextRetryTime())
                .createTime(agentTask.getCreateTime())
                .build();
    }

    @Scheduled(fixedDelay = 10000)
    public void retryFailedTasks() {
        String statusIndexKey = TASK_STATUS_INDEX_PREFIX + AgentFailTypeEnum.SYSTEM_RETRYABLE.getValue();
        Set<String> taskIds = stringRedisTemplate.opsForSet().members(statusIndexKey);
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }

        for (String taskId : taskIds) {
            AgentTask task = agentTaskRedisTemplate.opsForValue().get(AgentTaskConstant.AGENT_TASK_PREFIX + taskId);
            if (task == null) {
                continue;
            }
            if (!AgentTaskStatusEnum.RETRY_WAITING.getValue().equals(task.getTaskStatus())) {
                continue;
            }
            if (task.getRetryCount() >= 5) {
                continue;
            }
            if (task.getNextRetryTime() != null && task.getNextRetryTime().isAfter(LocalDateTime.now())) {
                continue;
            }
            autoRetry(task);
        }
    }

    private void executeMainTask(String taskId, String threadId, String appId, String userId, String message) {
        try {
            agentTaskService.updateStatus(taskId, RUNNING);
            agentTaskStreamManager.publishProgress(taskId, threadId, appId, "RUNNING",
                    "analyzing request and calling model", "app_creator_agent");
            SystemOutput output = agentAppCreator.chatWithUserId(message, threadId, appId, userId, taskId);
            handleTaskOutput(taskId, threadId, appId, userId, output, "generated by agent task");
            log.info("Agent task succeeded, taskId={}, threadId={}, appId={}, userId={}",
                    taskId, threadId, appId, userId);
        } catch (Exception e) {
            agentTaskService.markFailed(taskId, e);
            log.error("Agent task failed, taskId={}, threadId={}, appId={}, userId={}",
                    taskId, threadId, appId, userId, e);
        }
    }

    private void autoRetry(AgentTask task) {
        agentTaskService.updateStatus(task.getTaskId(), AgentTaskStatusEnum.RETRY_WAITING);
        log.info("Auto retry task, taskId={}, threadId={}, appId={}, retryCount={}",
                task.getTaskId(), task.getThreadId(), task.getAppId(), task.getRetryCount());

        agentTaskExecutor.submitAgentTask(task.getTaskId(), () -> {
            try {
                agentTaskService.updateStatus(task.getTaskId(), AgentTaskStatusEnum.RUNNING);
                String originalMessage = agentTaskService.getOriginalMessage(task.getTaskId());
                if (StrUtil.isBlank(originalMessage)) {
                    throw new IllegalStateException("missing retry input for task");
                }
                App app = appService.getById(task.getAppId());
                if (app == null || StrUtil.isBlank(app.getUserId())) {
                    throw new IllegalStateException("missing app owner for retry task");
                }
                agentTaskStreamManager.publishProgress(task.getTaskId(), task.getThreadId(), task.getAppId(), "RUNNING",
                        "retrying task with original input", "app_creator_agent");
                SystemOutput output = agentAppCreator.chatWithUserId(
                        originalMessage,
                        task.getThreadId(),
                        task.getAppId(),
                        app.getUserId(),
                        task.getTaskId());
                handleTaskOutput(task.getTaskId(), task.getThreadId(), task.getAppId(), app.getUserId(),
                        output, "generated by agent retry task");
            } catch (Exception e) {
                agentTaskService.markFailed(task.getTaskId(), e);
            }
        });
    }

    public void manualRetry(AgentTask task) {
        log.info("Manual retry task, taskId={}, threadId={}, appId={}, retryCount={}",
                task.getTaskId(), task.getThreadId(), task.getAppId(), task.getRetryCount());
        task.setRetryCount(0);
        task.setTaskStatus(AgentTaskStatusEnum.RETRY_WAITING.getValue());
        task.setNextRetryTime(LocalDateTime.now());
        agentTaskRedisTemplate.opsForValue().set(AgentTaskConstant.AGENT_TASK_PREFIX + task.getTaskId(), task);
        autoRetry(task);
    }

    private void handleTaskOutput(
            String taskId,
            String threadId,
            String appId,
            String userId,
            SystemOutput output,
            String versionMessage
    ) throws Exception {
        if (output.getAgentResponse() != null
                && output.getAgentResponse().getStructuredReply() != null
                && output.getAgentResponse().getStructuredReply().getFiles() != null) {
            agentTaskStreamManager.publishProgress(taskId, threadId, appId, "RUNNING",
                    "writing generated files", "app_creator_agent");
            codeFileSaverUtil.writeFilesToLocal(output.getAgentResponse().getStructuredReply().getFiles(), appId);
            appVersionService.createVersionSnapshot(appId, AppVersionSourceEnum.GENERATED.getValue(), versionMessage, null);
        }
        if (output.getAgentResponse() != null
                && output.getAgentResponse().getCodeOptimizationInput() != null) {
            agentTaskStreamManager.publishProgress(taskId, threadId, appId, "RUNNING",
                    "submitting optimization task", "app_creator_agent");
            agentTaskExecutor.submitOptimizationTask(
                    output.getAgentResponse().getCodeOptimizationInput(),
                    threadId,
                    appId,
                    userId
            );
        }
        agentTaskService.saveFinalOutput(taskId, output);
        agentTaskService.updateStatus(taskId, SUCCEEDED);
    }

    private void validateUserInput(String input) {
        if (StrUtil.isBlank(input)) {
            throw new BusinessException("input is blank", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("input is too long", ErrorCode.PARAMS_ERROR);
        }
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("input contains sensitive content", ErrorCode.PARAMS_ERROR);
        }
    }
}
