package com.xiaorui.agentapplicationcreator.agent.tool;

import com.xiaorui.agentapplicationcreator.agent.rag.model.SpecSearchRequest;
import com.xiaorui.agentapplicationcreator.agent.rag.model.SpecSearchResult;
import com.xiaorui.agentapplicationcreator.agent.rag.service.SpecSearchService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @description: 文档搜索工具 <a href="https://java2ai.com/docs/frameworks/agent-framework/advanced/rag#agentic-rag">...</a>
 * @author: xiaorui
 * @date: 2026-01-17 17:51
 **/
@Component
public class DocumentSearchTool {

    @Resource
    private SpecSearchService specSearchService;

    public record Request(String query, String generationMode, String stage) {}

    public record Response(String content) {}

    public Response search(Request request) {
        SpecSearchResult result = specSearchService.search(
                new SpecSearchRequest(request.query(), request.generationMode(), request.stage())
        );
        return new Response(result.content());
    }
}
