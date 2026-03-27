package com.xiaorui.agentapplicationcreator.agent.rag.service.impl;

import com.xiaorui.agentapplicationcreator.agent.rag.metadata.service.MetadataManager;
import com.xiaorui.agentapplicationcreator.agent.rag.model.SpecSearchRequest;
import com.xiaorui.agentapplicationcreator.agent.rag.model.SpecSearchResult;
import com.xiaorui.agentapplicationcreator.agent.rag.service.SpecSearchService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 默认规范检索服务
 * @author: xiaorui
 * @date: 2026-03-26 21:01
 **/
@Service
public class DefaultSpecSearchServiceImpl implements SpecSearchService {

    @Resource
    private VectorStore documentVectorStore;

    @Resource
    private MetadataManager metadataManager;

    @Override
    public SpecSearchResult search(SpecSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            return new SpecSearchResult("", List.of());
        }
        // 初始化 spec 数据
        metadataManager.ensureSpecsInitialized();
        List<String> candidateSpecIds = metadataManager.findCandidateSpecIds(request.generationMode(), request.stage());
        if (candidateSpecIds == null || candidateSpecIds.isEmpty()) {
            return new SpecSearchResult("", List.of());
        }
        // 构建搜索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.query())
                .topK(2)
                .filterExpression(
                        "specId in [" +
                                candidateSpecIds.stream()
                                        .map(id -> "'" + id + "'")
                                        .collect(Collectors.joining(",")) +
                                "]"
                )
                .build();
        // 执行相似度搜索
        List<Document> docs = documentVectorStore.similaritySearch(searchRequest);
        if (docs == null || docs.isEmpty()) {
            return new SpecSearchResult("", List.of());
        }
        // 提取匹配的 specId
        List<String> matchedSpecIds = docs.stream()
                .map(Document::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // 拼接匹配的 spec 内容
        String combinedContent = matchedSpecIds.stream()
                .map(metadataManager::loadSpecContent)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));

        return new SpecSearchResult(combinedContent, matchedSpecIds);
    }
}
