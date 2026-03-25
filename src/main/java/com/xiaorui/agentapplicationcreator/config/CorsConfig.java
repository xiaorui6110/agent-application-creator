package com.xiaorui.agentapplicationcreator.config;

import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Resource
    private AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        applyPolicy(registry.addMapping("/api/**"), appProperties.getCors().getApi());
        applyPolicy(registry.addMapping("/static/**"), appProperties.getCors().getStatics());
    }

    private void applyPolicy(CorsRegistration registration, AppProperties.CorsPolicy policy) {
        registration
                .allowCredentials(policy.isAllowCredentials())
                .allowedOriginPatterns(policy.getAllowedOriginPatterns().toArray(String[]::new))
                .allowedMethods(policy.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(policy.getAllowedHeaders().toArray(String[]::new))
                .exposedHeaders(policy.getExposedHeaders().toArray(String[]::new))
                .maxAge(policy.getMaxAge());
    }
}