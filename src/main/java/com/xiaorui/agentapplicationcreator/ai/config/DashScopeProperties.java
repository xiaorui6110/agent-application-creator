package com.xiaorui.agentapplicationcreator.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description: 获取 API KEY
 * @author: xiaorui
 * @date: 2025-12-10 14:55
 **/
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.dashscope")
public class DashScopeProperties {
    private String apiKey;
}