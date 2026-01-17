package com.xiaorui.agentapplicationcreator.agent.rag.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 文档向量存储配置（这样就相当于有了基于内存的向量库，暂时不引入第三方库）
 * @author: xiaorui
 * @date: 2026-01-17 16:33
 **/
@Configuration
public class DocumentVectorStoreConfig {

    @Bean
    VectorStore documentVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 为了实现方便，先使用 Spri؜ng AI 内置的、基于内存读写的向量数据库 SimpleVectorStore 来保存文档
        return SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
    }

}
