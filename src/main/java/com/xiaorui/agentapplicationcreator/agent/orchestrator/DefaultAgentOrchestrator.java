package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import cn.hutool.core.lang.UUID;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.dto.AgentTaskStatus;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.enums.AgentFailTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatusEnum.RUNNING;

/**
 * @description: 默认 Agent 编排器
 * @author: xiaorui
 * @date: 2026-01-19 18:08
 **/
@Component
@Slf4j
public class DefaultAgentOrchestrator implements AgentOrchestrator {

    private static final String TASK_STATUS_INDEX_PREFIX = "task_status:";

    @Resource
    private AgentAppCreator appCreatorAgent;

    @Resource
    private AgentTaskExecutor agentTaskExecutor;

    @Resource
    private AgentTaskService agentTaskService;

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

        String taskId = UUID.randomUUID().toString();

        // 初始化任务状态
        agentTaskService.initTask(taskId, threadId, appId);
        // 提交任务
        agentTaskExecutor.submitAgentTask(taskId, () -> {

            try {
                // 更新状态 - 运行中
                agentTaskService.updateStatus(taskId, RUNNING);
                // 执行 Agent 任务
                SystemOutput output = appCreatorAgent.chat(message, threadId, appId);
                // 保存文件
                CodeFileSaverUtil.writeFilesToLocal(output.getAgentResponse().getStructuredReply().getFiles(), appId);
                // 保存最终输出
                agentTaskService.saveFinalOutput(taskId, output);
                // 提交优化任务
                agentTaskExecutor.submitOptimizationTask(output.getAgentResponse().getCodeOptimizationInput(), threadId, appId);

            } catch (Exception e) {
                // 更新状态 - 失败
                agentTaskService.markFailed(taskId, e);
            }
        });
        AgentTask agentTask = agentTaskService.getByTaskId(taskId);
        // 返回入队状态
        return AgentTaskStatus.builder()
                .taskId(taskId)
                .threadId(threadId)
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
            if (task.getTaskStatus() != AgentTaskStatusEnum.FAILED) {
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
                                appCreatorAgent.chat(task.getTaskResult().getIntentSummary(),
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
        task.setTaskStatus(AgentTaskStatusEnum.RETRY_WAITING);
        task.setNextRetryTime(LocalDateTime.now());

        agentTaskRedisTemplate.opsForValue().set("retry:task:" + task.getTaskId(), task);

        // 真正执行（其实流程还是一样的）
        autoRetry(task);
    }

}

