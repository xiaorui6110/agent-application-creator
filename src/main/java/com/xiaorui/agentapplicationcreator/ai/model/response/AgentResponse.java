package com.xiaorui.agentapplicationcreator.ai.model.response;

import lombok.Data;

/**
 * @description: 智能体回复
 * @author: xiaorui
 * @date: 2025-12-12 15:00
 **/
@Data
public class AgentResponse {

    /**
     * 线程id
     */
    private String threadId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 回复
     */
    private String reply;
    /**
     * 时间戳
     */
    private long timestamp;

    //private List<ToolCall> toolCalls; // 如果 agent 使用工具
    //private boolean fromCache;        // 是否历史缓存响应

}
