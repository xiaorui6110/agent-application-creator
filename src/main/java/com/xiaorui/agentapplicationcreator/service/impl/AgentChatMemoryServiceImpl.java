package com.xiaorui.agentapplicationcreator.service.impl;

import com.mongodb.MongoWriteException;
import com.xiaorui.agentapplicationcreator.infrastructure.repository.AgentChatMessageRepository;
import com.xiaorui.agentapplicationcreator.model.entity.AgentChatMessage;
import com.xiaorui.agentapplicationcreator.service.AgentChatMemoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: Agent 对话消息 服务层实现。
 * @author: xiaorui
 * @date: 2025-12-15 15:03
 **/
@Slf4j
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
        // 保存信息失败时：不会返回任何值，而是直接抛出 MongoWriteException/MongoException 等异常
        try {
            repository.save(message);
        } catch (MongoWriteException e ) {
            log.error("MongoDB 保存对话消息失败：{}", e.getMessage());
        }
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

