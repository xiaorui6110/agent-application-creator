package com.xiaorui.agentapplicationcreator.agent.rag.metadata.service.impl;

import com.alibaba.dashscope.utils.JsonUtils;
import com.xiaorui.agentapplicationcreator.agent.rag.metadata.entity.SpecMetadata;
import com.xiaorui.agentapplicationcreator.agent.rag.metadata.loader.DocumentLoader;
import com.xiaorui.agentapplicationcreator.agent.rag.metadata.service.MetadataManager;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: metadata 管理器实现
 * @author: xiaorui
 * @date: 2026-01-17 21:26
 **/
@Service
public class MetadataManagerImpl implements MetadataManager {

    /**
     * 懒加载 + 幂等初始化
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Resource
    private DocumentLoader documentLoader;

    @Resource
    private VectorStore documentVectorStore;

    @Resource
    private StringRedisTemplate redisTemplate;

    /**
     * 确保 spec 已写入 Redis + VectorStore
     */
    @Override
    public void ensureSpecsInitialized() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        // 1️. 从 classpath 加载 spec markdown
        List<Document> documents = documentLoader.loadMarkdowns();

        if (documents == null || documents.isEmpty()) {
            return;
        }

        for (Document doc : documents) {

            String specId = doc.getId();
            String content = doc.getText();

            // 2️. 构建 SpecMetadata（从 document metadata 中提取）
            SpecMetadata metadata = SpecMetadata.from(doc.getMetadata());

            // 3️. 写入 Redis（metadata + content）
            String redisKey = "spec:" + specId;

            redisTemplate.opsForHash().put(redisKey, "metadata", JsonUtils.toJson(metadata));

            redisTemplate.opsForHash().put(redisKey, "content", content);

            // 4️. 写入 VectorStore（只存 embedding + specId）
            Document vectorDoc = new Document(specId, content, Map.of("specId", specId));

            documentVectorStore.add(List.of(vectorDoc));
        }
    }


    /**
     * 查找候选规范
     * @param generationMode 生成模式
     * @param stage 阶段
     * @return 候选规范ID
     */
    @Override
    public List<String> findCandidateSpecIds(String generationMode, String stage) {

        Set<String> keys = redisTemplate.keys("spec:*");
        // 先用 Redis 做 metadata 过滤
        return keys.stream()
                .filter(key -> {
                    String metaJson = (String) redisTemplate
                            .opsForHash()
                            .get(key, "metadata");

                    SpecMetadata meta = JsonUtils.fromJson(metaJson, SpecMetadata.class);

                    return meta.getGenerationMode().equals(generationMode)
                            && meta.getStage().contains(stage)
                            && "ACTIVE".equals(meta.getStatus());
                })
                .toList();
    }

    /**
     * 从 Redis 回表获取 spec 原文
     * @param specId 规范ID
     * @return 规范原文
     */
    @Override
    public String loadSpecContent(String specId) {
        if (specId == null || specId.isBlank()) {
            return null;
        }

        return (String) redisTemplate.opsForHash().get("spec:" + specId, "content");
    }


}
