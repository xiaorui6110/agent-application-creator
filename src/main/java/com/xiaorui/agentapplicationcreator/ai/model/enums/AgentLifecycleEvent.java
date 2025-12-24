package com.xiaorui.agentapplicationcreator.ai.model.enums;

import lombok.Getter;

/**
 * @description: Agent 生命周期事件
 * @author: xiaorui
 * @date: 2025-12-24 17:02
 **/
@Getter
public enum AgentLifecycleEvent {
    /**
     * Agent 生命周期事件
     */
    BEFORE_AGENT,
    AFTER_AGENT,
    ON_ERROR
}
