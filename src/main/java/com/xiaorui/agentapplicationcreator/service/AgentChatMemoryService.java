package com.xiaorui.agentapplicationcreator.service;

import com.xiaorui.agentapplicationcreator.model.entity.AgentChatMessage;

import java.util.List;

/**
 * @description: Agent 对话消息 服务层。
 * @author: xiaorui
 * @date: 2025-12-15 15:00
 **/
public interface AgentChatMemoryService {

    /**
     * 保存对话消息
     *
     * @param message 对话消息
     */
    void saveMessage(AgentChatMessage message);

    /**
     * 获取对话消息
     *
     * @param userId 用户ID
     * @param threadId 对话线程ID
     * @return 对话消息列表
     */
    List<AgentChatMessage> getConversation(String userId, String threadId);
}
