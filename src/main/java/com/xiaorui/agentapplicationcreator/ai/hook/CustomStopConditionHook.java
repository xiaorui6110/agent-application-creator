package com.xiaorui.agentapplicationcreator.ai.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.dashscope.common.Message;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @description: TODO 自定义停止条件：基于状态判断是否继续（看情况使用，大概率用不上）
 * @author: xiaorui
 * @date: 2025-12-11 14:03
 **/
@HookPositions({HookPosition.BEFORE_MODEL})
public class CustomStopConditionHook extends ModelHook {

    @Override
    public String getName() {
        return "custom_stop_condition";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        // 检查是否找到答案，展示使用 OverAllState
        boolean answerFound = (Boolean) state.value("answer_found").orElse(false);
        // 检查错误次数，展示使用 RunnableConfig
        int errorCount = (Integer) config.context().get("error_count");

        // 找到答案或错误过多时停止
        if (answerFound || errorCount > 3) {
            List<AssistantMessage> messages = new ArrayList<AssistantMessage>(
                    (List<AssistantMessage>) state.value("messages").orElse(new ArrayList<>())
            );
            messages.add(new AssistantMessage(
                    answerFound ? "已找到答案，Agent 执行完成。"
                            : "错误次数过多 (" + errorCount + ")，Agent 执行终止。"
            ));
            return CompletableFuture.completedFuture(Map.of("messages", messages));
        }

        return CompletableFuture.completedFuture(Map.of());
    }

    /*
        private RunnableConfig(Builder builder) {
        this.threadId = builder.threadId;
        this.checkPointId = builder.checkPointId;
        this.nextNode = builder.nextNode;
        this.streamMode = builder.streamMode;
        this.metadata = (Map)Optional.ofNullable(builder.metadata()).map(Map::copyOf).orElse((Object)null);
        this.interruptedNodes = new ConcurrentHashMap();
        this.store = builder.store;
        this.context = builder.context;
    }
     */

}