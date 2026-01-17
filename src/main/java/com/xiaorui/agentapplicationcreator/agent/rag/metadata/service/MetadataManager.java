package com.xiaorui.agentapplicationcreator.agent.rag.metadata.service;

import java.util.List;

/**
 * @description: metadata 管理器
 * @author: xiaorui
 * @date: 2026-01-17 21:24
 **/
public interface MetadataManager {

    /**
     * 确保 spec 已写入 Redis + VectorStore
     * 必须是幂等的
     */
    void ensureSpecsInitialized();

    /**
     * 基于 metadata 过滤 specId
     * @param generationMode 生成模式
     * @param stage 阶段
     * @return 候选规范ID
     */
    List<String> findCandidateSpecIds(String generationMode, String stage);

    /**
     * 从 Redis 回表获取 spec 原文
     * @param specId 规范ID
     * @return 规范原文
     */
    String loadSpecContent(String specId);
}
