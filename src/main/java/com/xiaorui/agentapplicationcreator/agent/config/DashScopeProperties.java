package com.xiaorui.agentapplicationcreator.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description: 从 yml 配置文件中获取 API KEY（最好使用 .env 系统配置文件获取， `.apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))` ）
 * @author: xiaorui
 * @date: 2025-12-10 14:55
 **/
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.dashscope")
public class DashScopeProperties {
    private String apiKey;

    private Chat chat = new Chat();

    @Data
    public static class Chat {
        private Options options = new Options();
    }

    @Data
    public static class Options {
        private String model;
        private Boolean stream;
        private Boolean incrementalOutput;
        private Double temperature;
    }
}
