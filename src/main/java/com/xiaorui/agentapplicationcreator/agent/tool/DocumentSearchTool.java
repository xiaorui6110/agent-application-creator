package com.xiaorui.agentapplicationcreator.agent.tool;

import com.xiaorui.agentapplicationcreator.agent.rag.metadata.service.MetadataManager;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 文档搜索工具 <a href="https://java2ai.com/docs/frameworks/agent-framework/advanced/rag#agentic-rag">...</a>
 * @author: xiaorui
 * @date: 2026-01-17 17:51
 **/
@Component
public class DocumentSearchTool {

    @Resource
    private VectorStore documentVectorStore;

    @Resource
    private MetadataManager metadataManager;

    public record Request(String query, String generationMode, String stage) {}

    public record Response(String content) {}

    public Response search(Request request) {

        // 1. 确保 Spec 已初始化（只会执行一次，幂等）
        metadataManager.ensureSpecsInitialized();

        // 2. 基于 metadata 过滤候选 specId
        List<String> candidateSpecIds = metadataManager.findCandidateSpecIds(request.generationMode(), request.stage());

        if (candidateSpecIds.isEmpty()) {
            return new Response("");
        }

        // 3. 向量检索（带 filter）
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

        List<Document> docs = documentVectorStore.similaritySearch(searchRequest);

        if (docs.isEmpty()) {
            return new Response("");
        }

        // 4. 回表 Redis，获取完整 spec 内容
        String combinedContent = docs.stream()
                .map(doc -> metadataManager.loadSpecContent(doc.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));

        return new Response(combinedContent);
    }
}
