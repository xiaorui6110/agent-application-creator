package com.xiaorui.agentapplicationcreator.ai.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @description: AgentHook - 在 Agent 开始/结束时执行，每次Agent调用只会运行一次
 * @author: xiaorui
 * @date: 2025-12-11 13:46
 **/
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class LoggingHook extends AgentHook {
    @Override
    public String getName() { return "logging"; }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        System.out.println("The agent has started executing, please be patient.");
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        System.out.println("Agent has completed the task, let's see what the response is.");
        return CompletableFuture.completedFuture(Map.of());
    }


    /*

    ✔️ 适合 Hook 的典型动作：和业务无关、和 Agent 强相关、可以失败，但不影响主流程

        ✔ 自动保存生成代码到本地
        ✔ 记录生成快照（version）
        ✔ 更新应用状态（GENERATED）
        ✔ 写审计日志
        ✔ 触发异步部署
        ✔ 统计指标（tokens / cost）

    ❌ 什么不该 Hook？：需要即时返回给用户的逻辑、有严格顺序依赖的逻辑、失败就必须中断主流程的逻辑

        ✘ 参数校验
        ✘ 权限判断
        ✘ Agent 选择

     */
}