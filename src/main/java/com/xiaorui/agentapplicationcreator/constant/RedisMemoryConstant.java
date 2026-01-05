package com.xiaorui.agentapplicationcreator.constant;

/**
 * @description: redis 对话历史信息内存key
 * @author: xiaorui
 * @date: 2025-12-29 16:45
 **/
public interface RedisMemoryConstant {

    /**
     * 用户对话信息内存key前缀
     */
    String USER_MESSAGE_MEMORY_PREFIX = "xiaorui_user_memory:";

    /**
     * AI对话信息内存key前缀
     */
    String AI_MESSAGE_MEMORY_PREFIX = "xiaorui_ai_memory:";


}
