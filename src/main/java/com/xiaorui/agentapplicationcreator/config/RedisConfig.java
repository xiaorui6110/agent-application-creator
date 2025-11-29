package com.xiaorui.agentapplicationcreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @description: Redis 配置
 * @author: xiaorui
 * @date: 2025-11-29 16:08
 **/
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 设置 Key 的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 设置 Value 的序列化方式（JSON）
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

}
