package com.xiaorui.agentapplicationcreator.agent.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @description: AgentHook - 在 Agent 整体执行的开始和结束时执行
 * @author: xiaorui
 * @date: 2025-12-11 13:46
 **/
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class LoggingHook extends AgentHook {
    @Override
    public String getName() { return "logging_hook"; }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        System.out.println("The agent has started executing, please be patient.");
        // 可以初始化资源、记录开始时间等
        return CompletableFuture.completedFuture(Map.of("start_time", System.currentTimeMillis()));
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        System.out.println("Agent has completed the task, let's see what the response is.");
        // 可以清理资源、计算执行时间等
        Optional<Object> startTime = state.value("start_time");
        if (startTime.isPresent()) {
            long duration = System.currentTimeMillis() - (Long) startTime.get();
            System.out.println("Model response time: " + duration + "ms");
        }
        return CompletableFuture.completedFuture(Map.of());
    }
}