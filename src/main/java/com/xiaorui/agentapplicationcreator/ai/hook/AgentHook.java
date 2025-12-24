package com.xiaorui.agentapplicationcreator.ai.hook;

import com.xiaorui.agentapplicationcreator.ai.context.AgentContext;
import com.xiaorui.agentapplicationcreator.ai.model.enums.AgentLifecycleEvent;

import java.util.Map;

/**
 * @description: Agent 生命周期 Hook
 * @author: xiaorui
 * @date: 2025-12-24 17:01
 **/
public interface AgentHook {

    AgentLifecycleEvent event();

    void handle(AgentContext context, Map<String, String> files);
}

