package com.xiaorui.agentapplicationcreator.ai.model.dto;

import lombok.Data;

/**
 * @description: 智能体请求
 * @author: xiaorui
 * @date: 2025-12-12 15:00
 **/
@Data
public class CallAgentRequest {

    /**
     * 消息
     */
    private String message;

    /**
     * 线程id（threadId 是给定对话的唯一标识符，使用 threadId 维护对话上下文）
     */
    private String threadId;

    //private String locale;     // 语言
    //private boolean stream;    // 是否流式
    //private Map<String,Object> context;  // 扩展使用

}
