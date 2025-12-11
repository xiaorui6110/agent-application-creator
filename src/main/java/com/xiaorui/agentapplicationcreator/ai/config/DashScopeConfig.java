package com.xiaorui.agentapplicationcreator.ai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
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
                // TODO 后期按需调整设置（ChatModel）
                //.defaultOptions(DashScopeChatOptions.builder()
                //    // 控制随机性（控制输出的随机性（0.0-1.0），值越高越有创造性）
                //    .withTemperature(0.7)
                //    // 最大输出长度（限制单次响应的最大 token 数）
                //    .withMaxToken(2000)
                //    // 核采样参数（核采样，控制输出的多样性）
                //    .withTopP(0.9)
                //    .enableThinking(true)
                //    .build())
                .build();
    }
}