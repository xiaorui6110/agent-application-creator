package com.xiaorui.agentapplicationcreator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.dev33.satoken.json.SaJsonTemplateForJackson.DATE_TIME_PATTERN;

/**
 * @description: Redis 配置
 * @author: xiaorui
 * @date: 2025-11-29 16:08
 **/
@Configuration
public class RedisConfig {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 配置 RedisTemplate，支持 LocalDateTime 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 1. 配置 Jackson ObjectMapper，启用 Java 8 日期时间支持
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 2. 配置 LocalDateTime 的序列化/反序列化器（指定格式化样式）
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
        );
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
        );
        javaTimeModule.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        // 3. 注册 JavaTimeModule，启用 Java 8 日期时间支持
        objectMapper.registerModule(javaTimeModule);
        // 可选：关闭时间戳序列化（默认会将日期序列化为时间戳，关闭后用指定格式字符串）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. 配置 Redis 序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 5. 设置 Key 和 Value 的序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // Value 用 JSON 序列化（支持 LocalDateTime）
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        // Hash Key 用字符串序列化
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // Hash Value 用 JSON 序列化
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 注册 JavaTimeModule，专门处理 Java 8 日期时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 2. 配置 LocalDateTime 的序列化器和反序列化器（指定统一格式）
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        );
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        );
        javaTimeModule.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        // 3. 注册模块到 ObjectMapper（核心步骤，缺一不可）
        objectMapper.registerModule(javaTimeModule);

        // 4. 可选配置：关闭日期时间以时间戳格式序列化（默认开启，关闭后用上面指定的字符串格式）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 5. 可选配置：忽略未知字段（避免反序列化时因字段不一致报错）
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }


    /**
     * 自定义 RedisTemplate<String, AgentTask>
     */
    @Bean
    public RedisTemplate<String, AgentTask> agentTaskRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, AgentTask> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 采用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 步骤1：创建配置好的 ObjectMapper，支持 LocalDateTime 序列化/反序列化
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置 LocalDateTime 的序列化器和反序列化器（指定统一格式）
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        );
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        );
        javaTimeModule.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        // 注册 JavaTimeModule（核心：启用 Java 8 日期时间支持）
        objectMapper.registerModule(javaTimeModule);
        // 可选：关闭日期以时间戳格式序列化，使用上面指定的字符串格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 可选：忽略未知字段，避免反序列化时因字段不一致报错
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 步骤2：使用双参数构造函数创建 Jackson2JsonRedisSerializer
        Jackson2JsonRedisSerializer<AgentTask> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, AgentTask.class);

        // 步骤3：value 采用配置后的 Jackson 序列化
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }
}
