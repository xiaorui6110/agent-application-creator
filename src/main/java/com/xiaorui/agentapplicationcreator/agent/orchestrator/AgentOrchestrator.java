package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import com.xiaorui.agentapplicationcreator.agent.model.dto.AgentTaskStatus;

import java.io.IOException;

/**
 * @description: Agent 编排器
 * @author: xiaorui
 * @date: 2026-01-19 18:07
 **/
public interface AgentOrchestrator {

    /**
     * 处理用户消息
     *
     * @param message 用户消息
     * @param threadId 对话线程 ID
     * @param appId 应用 ID
     * @return Agent 任务状态
     * @throws IOException IO 异常
     */
    AgentTaskStatus handleUserMessage(String message, String threadId, String appId) throws IOException;

}
