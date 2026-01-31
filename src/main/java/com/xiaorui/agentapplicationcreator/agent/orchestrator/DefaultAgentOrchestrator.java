package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.dto.AgentTaskStatus;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.entity.AgentChatMessage;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.service.*;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum.RUNNING;
import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum.SUCCEEDED;

/**
 * @description: 默认 Agent 编排器
 * @author: xiaorui
 * @date: 2026-01-19 18:08
 **/
@Component
@Slf4j
public class DefaultAgentOrchestrator implements AgentOrchestrator {

    private static final String TASK_STATUS_INDEX_PREFIX = "task_status:";

    private final static Integer MAX_INPUT_LENGTH = 2000;

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
    private AgentChatMemoryService agentChatMemoryService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private RedisTemplate<String, AgentTask> agentTaskRedisTemplate;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

    /**
     * 处理用户消息
     *
     * @param message 用户消息
     * @param threadId 对话线程 ID
     * @param appId 应用 ID
     * @return Agent 任务状态
     */
    @Override
    public AgentTaskStatus handleUserMessage(String message, String threadId, String appId) {

        // 输入校验
        validateUserInput(message);

        String taskId = UUID.randomUUID().toString();
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // threadId：非空沿用旧的 / 空的生成新的
        String updatedThreadId = Optional.ofNullable(threadId)
                .filter(StringUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());
        log.info("taskId={}, threadId={}, appId={}", taskId, updatedThreadId, appId);
        // 强制绑定 userId 与 threadId（threadId 由于上一步的操作一定非空）
        if (!threadBelongsToUser(userId, updatedThreadId)) {
            throw new BusinessException("非法对话 ID，已阻断访问", ErrorCode.FORBIDDEN_ERROR);
        }
        log.info("agent task started");
        // 初始化任务状态
        agentTaskService.initTask(taskId, updatedThreadId, appId);
        // 保存用户输入（底层已存在校验插入成功与否的错误处理，此处不做追加处理 log）（暂时先把 MongoDB 的保存操作注释掉）
        //agentChatMemoryService.saveMessage(buildMessage(userId, threadId, appId,"user", message));
        chatHistoryService.saveChatHistory(appId, userId, message, "user");
        
        // 提交任务（在主线程中捕获 userId，避免异步线程中 Sa-Token 上下文丢失）
        String finalUserId = userId;
        agentTaskExecutor.submitAgentTask(taskId, () -> {

            try {
                // 更新状态 - 运行中
                agentTaskService.updateStatus(taskId, RUNNING);
                // 执行 Agent 任务（传入 userId，避免在异步线程中调用 SecurityUtil）
                SystemOutput output = agentAppCreator.chatWithUserId(message, updatedThreadId, appId, finalUserId);
                // 需求明确阶段 StructuredReply 为空，不保存文件
                if (output.getAgentResponse().getStructuredReply() != null) {
                    CodeFileSaverUtil.writeFilesToLocal(output.getAgentResponse().getStructuredReply().getFiles(), appId);
                }
                // 保存最终输出
                agentTaskService.saveFinalOutput(taskId, output);
                // 更新状态 - 成功
                agentTaskService.updateStatus(taskId, SUCCEEDED);
                // 提交优化任务（TODO 由于项目不成熟，还是将代码优化部分功能暂时移除吧）
                // 异步方法异常: codeOptimizeAsync - Cannot invoke "com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput.getAppId()" because "codeOptimizationInput" is null
                //agentTaskExecutor.submitOptimizationTask(output.getAgentResponse().getCodeOptimizationInput(), updatedThreadId, appId, finalUserId);
                log.info("agent task finished");
            } catch (Exception e) {
                // 更新状态 - 失败
                agentTaskService.markFailed(taskId, e);
                log.error("agent task failed", e);
            }
        });
        AgentTask agentTask = agentTaskService.getByTaskId(taskId);
        // 返回入队状态
        return AgentTaskStatus.builder()
                .taskId(taskId)
                .threadId(updatedThreadId)
                .appId(appId)
                .taskStatus(agentTask.getTaskStatus())
                // 展示给用户的信息
                .message("任务已入队")
                .retryCount(agentTask.getRetryCount())
                .failType(agentTask.getFailType())
                .nextRetryTime(agentTask.getNextRetryTime())
                .createTime(agentTask.getCreateTime())
                .build();
    }


    /**
     * 重试失败任务（每 10 秒执行一次）（后续可以使用 Redis ZSet 优化）
     */
    @Scheduled(fixedDelay = 10000)
    public void retryFailedTasks() {
        // 获取所有状态为 SYSTEM_RETRYABLE 的任务
        String statusIndexKey = TASK_STATUS_INDEX_PREFIX + AgentFailTypeEnum.SYSTEM_RETRYABLE.getValue();
        // key=task_status:system_retryable，value=taskId
        Set<String> taskIds = stringRedisTemplate.opsForSet().members(statusIndexKey);

        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }

        for (String taskId : taskIds) {
            AgentTask task = agentTaskRedisTemplate.opsForValue().get(taskId);
            if (task == null) {
                continue;
            }
            if (!task.getTaskStatus().equals(AgentTaskStatusEnum.FAILED.getValue())) {
                continue;
            }
            if (task.getRetryCount() >= 5) {
                continue;
            }
            // 执行重试失败任务
            autoRetry(task);
        }
    }


    /**
     * 执行重试失败任务
     */
    private void autoRetry(AgentTask task) {

        agentTaskService.updateStatus(task.getTaskId(), AgentTaskStatusEnum.RETRY_WAITING);

        log.info("Auto retry: taskId={}, status={}", task.getTaskId(), task.getTaskStatus());

        agentTaskExecutor.submitAgentTask(
                task.getTaskId(), () -> {
                    try {
                        agentTaskService.updateStatus(task.getTaskId(), AgentTaskStatusEnum.RUNNING);

                        // 重跑主 Agent（这里有可优化的点，message，我放的是 intentSummary - Agent 对用户意图的理解摘要）
                        SystemOutput output =
                                agentAppCreator.chat(task.getTaskResult().getIntentSummary(),
                                        task.getThreadId(), task.getAppId());

                        agentTaskService.saveFinalOutput(task.getTaskId(), output);

                    } catch (Exception e) {
                        agentTaskService.markFailed(task.getTaskId(), e);
                    }
                }
        );
    }

    /**
     * 人工重试
     */
    public void manualRetry(AgentTask task) {

        log.info("Manual retry: taskId={}, status={}", task.getTaskId(), task.getTaskStatus());

        // 人工重放：强制重置 retry 计数
        task.setRetryCount(0);
        task.setTaskStatus(AgentTaskStatusEnum.RETRY_WAITING.getValue());
        task.setNextRetryTime(LocalDateTime.now());

        agentTaskRedisTemplate.opsForValue().set("retry:task:" + task.getTaskId(), task);

        // 真正执行（其实流程还是一样的）
        autoRetry(task);
    }


    /**
     * 判断对话是否属于当前用户（否则用户 A 可能传入用户 B 的 threadId，看到 B 的对话内容，非常危险）
     */
    private boolean threadBelongsToUser(String userId, String threadId) {
        // threadId 肯定非空，新 thread：绑定
        if (StrUtil.isNotBlank(threadId)) {
            userThreadBindService.bindThread(userId, threadId, "app_creator_agent");
            // 老 thread：校验归属
            if (!userThreadBindService.validateThreadOwner(userId, threadId)) {
                throw new BusinessException("非法对话 ID，已阻断访问", ErrorCode.FORBIDDEN_ERROR);
            }
        }
        return true;
    }

    /**
     * 用户输入校验：敏感词检测 ...
     */
    private void validateUserInput(String input) {
        if (StringUtil.isBlank(input)) {
            throw new BusinessException("输入不能为空", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("输入过长，请分段发送", ErrorCode.PARAMS_ERROR);
        }
        // 验证字符串是否包含敏感词（目前先使用第三方框架简单实现）
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("输入包含不适宜内容", ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 构建消息实体（MongoDB）
     */
    @NotNull
    private AgentChatMessage buildMessage(
            String userId,
            String threadId,
            String appId,
            String role,
            String content) {
        AgentChatMessage msg = new AgentChatMessage();
        msg.setUserId(userId);
        msg.setThreadId(threadId);
        msg.setAppId(appId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setAgentName("app_creator_agent");
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }

}

