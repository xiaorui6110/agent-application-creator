package com.xiaorui.agentapplicationcreator.ai.example;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;

/**
 * @description: 创建 Agent 示例
 * @author: xiaorui
 * @date: 2025-12-10 14:50
 **/
@Deprecated
public class AgentExample {

    public static void main(String[] args) throws Exception {
        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(TestApiKey.API_KEY)
                .build();
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .instruction("You are a helpful weather forecast assistant.")
                .build();

        // 运行 Agent
        agent.call("what is the weather in Hangzhou?");
    }
}
