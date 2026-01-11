package com.xiaorui.agentapplicationcreator.agent.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @description: 消息修剪 hook
 * @author: xiaorui
 * @date: 2026-01-11 11:13
 **/
@HookPositions({HookPosition.BEFORE_MODEL})
public class MessageTrimmingHook extends MessagesModelHook {
    private static final int MAX_MESSAGES = 10;

    @Override
    public String getName() {
        return "message_trimming";
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        // 如果消息数量超过限制，只保留最后 MAX_MESSAGES 条消息
        if (previousMessages.size() > MAX_MESSAGES) {
            List<Message> trimmedMessages = previousMessages.subList(
                    previousMessages.size() - MAX_MESSAGES,
                    previousMessages.size()
            );
            // 使用 REPLACE 策略替换所有消息
            return new AgentCommand(trimmedMessages, UpdatePolicy.REPLACE);
        }
        // 如果消息数量未超过限制，返回原始消息（不进行修改）
        return new AgentCommand(previousMessages);
    }

       /*
        关键规则：
            before_* hooks: 从第一个到最后一个
            after_* hooks: 从最后一个到第一个（逆序）
        */

}