package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import cn.hutool.core.lang.UUID;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatus.QUEUED;
import static com.xiaorui.agentapplicationcreator.enums.AgentTaskStatus.RUNNING;

/**
 * @description: 默认 Agent 编排器
 * @author: xiaorui
 * @date: 2026-01-19 18:08
 **/
@Component
@Slf4j
public class DefaultAgentOrchestrator implements AgentOrchestrator {

    @Resource
    private AgentAppCreator appCreatorAgent;

    @Resource
    private AgentTaskExecutor agentTaskExecutor;

    @Resource
    private AgentTaskService agentTaskService;

    @Override
    public SystemOutput handleUserMessage(String message, String threadId, String appId) {

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
        // 返回入队状态
        return SystemOutput.builder()
                .taskId(taskId)
                .taskStatus(QUEUED)
                .build();
    }
}

