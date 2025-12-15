package com.xiaorui.agentapplicationcreator.infrastructure.repository;

import com.xiaorui.agentapplicationcreator.model.entity.AgentChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @description: Agent 对话消息 服务层。
 * @author: xiaorui
 * @date: 2025-12-15 14:58
 **/
public interface AgentChatMessageRepository extends MongoRepository<AgentChatMessage, String> {

    /**
     * 根据用户 ID 和对话 ID 查询对话消息
     *
     * @param userId 用户 ID
     * @param threadId 对话线程 ID
     * @return 对话消息列表
     */
    List<AgentChatMessage> findByUserIdAndThreadIdOrderByTimestampAsc(String userId, String threadId);

    /**
     * 根据用户 ID 查询对话消息
     *
     * @param userId 用户 ID
     * @return 对话消息列表
     */
    List<AgentChatMessage> findByUserId(String userId);
}

