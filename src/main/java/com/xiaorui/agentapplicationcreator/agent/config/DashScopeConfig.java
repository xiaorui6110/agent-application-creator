package com.xiaorui.agentapplicationcreator.agent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: DashScope 配置
 * @author: xiaorui
 * @date: 2025-12-10 14:55
 **/
@Configuration
public class DashScopeConfig {

    private final DashScopeProperties props;

    public DashScopeConfig(DashScopeProperties props) {
        this.props = props;
    }

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(props.getApiKey())
                .build();
    }

    @Bean
    public ChatModel chatModel(DashScopeApi api) {
        return DashScopeChatModel.builder()
                .dashScopeApi(api)
                .build();
    }
}