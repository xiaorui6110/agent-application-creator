package com.xiaorui.agentapplicationcreator.infrastructure.memory;

import com.alibaba.cloud.ai.graph.checkpoint.savers.MongoSaver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;

import java.lang.reflect.Field;

/**
 * @description: 自定义 MongoSaver（替换默认写死的数据库名称）
 * @author: xiaorui
 * @date: 2025-12-15 15:22
 **/

public class CustomMongoSaver extends MongoSaver {

    public CustomMongoSaver(
            MongoClient mongoClient,
            ObjectMapper objectMapper,
            String databaseName
    ) {
        super(mongoClient, objectMapper);

        // 通过反射覆盖父类的 database 字段
        overrideDatabase(mongoClient, databaseName);
    }

    private void overrideDatabase(MongoClient mongoClient, String databaseName) {
        try {
            Field databaseField = MongoSaver.class.getDeclaredField("database");
            databaseField.setAccessible(true);
            databaseField.set(this, mongoClient.getDatabase(databaseName));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to override MongoSaver database", e);
        }
    }
}

