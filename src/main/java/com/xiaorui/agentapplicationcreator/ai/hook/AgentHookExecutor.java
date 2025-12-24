package com.xiaorui.agentapplicationcreator.ai.hook;

import com.xiaorui.agentapplicationcreator.ai.context.AgentContext;
import com.xiaorui.agentapplicationcreator.ai.model.enums.AgentLifecycleEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @description: Agent 生命周期 Hook 执行器
 * @author: xiaorui
 * @date: 2025-12-24 17:08
 **/
@Component
public class AgentHookExecutor {

    private final List<AgentHook> hooks;

    public AgentHookExecutor(List<AgentHook> hooks) {
        this.hooks = hooks;
    }

    public void fire(AgentLifecycleEvent event, AgentContext context, Map<String, String> files) {
        for (AgentHook hook : hooks) {
            if (hook.event() == event) {
                hook.handle(context, files);
            }
        }
    }
}

