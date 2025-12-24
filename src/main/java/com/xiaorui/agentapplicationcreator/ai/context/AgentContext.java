package com.xiaorui.agentapplicationcreator.ai.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: Agent 上下文
 * @author: xiaorui
 * @date: 2025-12-24 16:57
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 对话线程 ID
     */
    private String threadId;

    /**
     * 执行开始时间
     */
    private long startTime;

    /**
     * 扩展上下文（预留）
     */
    private Map<String, Object> attributes = new HashMap<>();
}
