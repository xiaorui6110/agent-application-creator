package com.xiaorui.agentapplicationcreator.ai.config;

import com.alibaba.cloud.ai.graph.checkpoint.savers.MongoSaver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.xiaorui.agentapplicationcreator.infrastructure.memory.CustomMongoSaver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: MongoSaver 配置
 * @author: xiaorui
 * @date: 2025-12-15 15:14
 **/
@Configuration
public class MongoSaverConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String agentMemoryDb;

    /**
     * MongoClient
     */
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * 专用 ObjectMapper（避免污染 Web ObjectMapper）
     */
    @Bean
    public ObjectMapper agentMemoryObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 让抽象 Message 能反序列化（之前炸的根源）
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        // Java Time 支持
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    /**
     * 自定义 MongoSaver
     */
    @Bean
    public MongoSaver mongoSaver(
            MongoClient mongoClient,
            @Qualifier("agentMemoryObjectMapper") ObjectMapper objectMapper
    ) {
        return new CustomMongoSaver(
                mongoClient,
                objectMapper,
                agentMemoryDb
        );
    }
}

