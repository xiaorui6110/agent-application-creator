package com.xiaorui.agentapplicationcreator.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * @description: Agent 对话消息实体类（mongodb）
 * @author: xiaorui
 * @date: 2025-12-15 14:55
 **/
@Document(collection = "agent_chat_message")
@Data
public class AgentChatMessage {

    /**
     * 对话 ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String msgId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 对话线程 ID
     */
    private String threadId;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 角色：user / assistant / system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * Agent 名称（固定 app_creator_agent）（仅用作标识）
     */
    private String agentName;

    /**
     * 时间戳（秒）
     */
    private Long timestamp;

    /**
     * 扩展信息（如工具调用、token 使用等）
     */
    private Map<String, Object> metadata;
}

