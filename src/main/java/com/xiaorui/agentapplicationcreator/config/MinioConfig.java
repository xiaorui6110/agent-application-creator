package com.xiaorui.agentapplicationcreator.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: MinIO 配置类
 * @author: xiaorui
 * @date: 2025-12-02 10:52
 **/
@Data
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * 注入 MinioClient 到 Spring 容器（单例）
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                // 可选：开启 HTTPS 时配置证书验证（如自签名证书）
                // .sslConfig(SSLConfig.builder().verifySsl(false).build())
                .build();
    }
}