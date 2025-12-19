package com.xiaorui.agentapplicationcreator.ai.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.dashscope.common.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @description: 修剪消息 TODO ModelHook - 在模型调用前后执行（例如：消息修剪），
 *              区别于AgentHook，ModelHook在一次agent调用中可能会调用多次，也就是每次 reasoning-acting 迭代都会执行
 *              适用场景：
 *                  超出上下文窗口的长期对话
 *                  具有大量历史记录的多轮对话
 *                  需要保留完整对话上下文的应用程序
 * @author: xiaorui
 * @date: 2025-12-11 13:47
 **/
public class MessageTrimmingHook extends ModelHook {
    /**
     * 最大消息数
     */
    private static final int MAX_MESSAGES = 10;

    @Override
    public String getName() {
        return "message_trimming";
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.BEFORE_MODEL};
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        Optional<Object> messagesOpt = state.value("messages");
        if (messagesOpt.isPresent()) {
            List<Message> messages = (List<Message>) messagesOpt.get();
            if (messages.size() > MAX_MESSAGES) {
                return CompletableFuture.completedFuture(Map.of("messages",
                        messages.subList(messages.size() - MAX_MESSAGES, messages.size())));
            }
        }
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        return CompletableFuture.completedFuture(Map.of());
    }

}