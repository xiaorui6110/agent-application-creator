package com.xiaorui.agentapplicationcreator.service.impl;

import com.xiaorui.agentapplicationcreator.model.entity.AgentChatMessage;
import com.xiaorui.agentapplicationcreator.infrastructure.repository.AgentChatMessageRepository;
import com.xiaorui.agentapplicationcreator.service.AgentChatMemoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: Agent 对话消息 服务层实现。
 * @author: xiaorui
 * @date: 2025-12-15 15:03
 **/
@Service
public class AgentChatMemoryServiceImpl implements AgentChatMemoryService {

    @Resource
    private AgentChatMessageRepository repository;

    /**
     * 保存对话消息
     *
     * @param message 对话消息
     */
    @Override
    public void saveMessage(AgentChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        repository.save(message);
    }

    /**
     * 获取对话消息
     *
     * @param userId 用户ID
     * @param threadId 对话线程ID
     * @return 对话消息列表
     */
    @Override
    public List<AgentChatMessage> getConversation(String userId, String threadId) {
        return repository.findByUserIdAndThreadIdOrderByTimestampAsc(userId, threadId);
    }
}

