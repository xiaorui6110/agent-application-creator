package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;

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
     * @return 系统输出
     * @throws IOException IO 异常
     */
    SystemOutput handleUserMessage(String message, String threadId, String appId) throws IOException;

}
