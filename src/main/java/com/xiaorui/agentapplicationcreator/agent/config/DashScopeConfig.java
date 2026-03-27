package com.xiaorui.agentapplicationcreator.agent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.xiaorui.agentapplicationcreator.manager.monitor.ObservableChatModel;
import com.xiaorui.agentapplicationcreator.service.ModelCallLogService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    public DashScopeChatModel dashScopeChatModel(DashScopeApi api) {
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .model(props.getChat().getOptions().getModel())
                .stream(props.getChat().getOptions().getStream())
                .incrementalOutput(props.getChat().getOptions().getIncrementalOutput())
                .temperature(props.getChat().getOptions().getTemperature())
                .build();
        return DashScopeChatModel.builder()
                .dashScopeApi(api)
                .defaultOptions(chatOptions)
                .build();
    }

    @Bean
    @Primary
    public ChatModel chatModel(DashScopeChatModel dashScopeChatModel, ModelCallLogService modelCallLogService) {
        return new ObservableChatModel(
                dashScopeChatModel,
                modelCallLogService,
                props.getChat().getOptions().getModel()
        );
    }
}
