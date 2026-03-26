package com.xiaorui.agentapplicationcreator.agent.rag.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @description: RAG 规范检索请求
 * @author: xiaorui
 * @date: 2026-03-26 21:00
 **/
public record SpecSearchRequest(
        @Schema(description = "检索查询语句") String query,
        @Schema(description = "生成模式") String generationMode,
        @Schema(description = "阶段") String stage
) {
}
