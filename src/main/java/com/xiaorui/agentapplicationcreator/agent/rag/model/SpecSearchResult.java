package com.xiaorui.agentapplicationcreator.agent.rag.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * @description: RAG 规范检索结果
 * @author: xiaorui
 * @date: 2026-03-26 21:00
 **/
public record SpecSearchResult(
        @Schema(description = "拼接后的规范内容") String content,
        @Schema(description = "命中的规范ID") List<String> matchedSpecIds
) {
}
