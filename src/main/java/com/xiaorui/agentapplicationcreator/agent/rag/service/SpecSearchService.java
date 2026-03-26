package com.xiaorui.agentapplicationcreator.agent.rag.service;

import com.xiaorui.agentapplicationcreator.agent.rag.model.SpecSearchRequest;
import com.xiaorui.agentapplicationcreator.agent.rag.model.SpecSearchResult;

/**
 * @description: 规范检索服务
 * @author: xiaorui
 * @date: 2026-03-26 21:01
 **/
public interface SpecSearchService {

    /**
     * 检索规范内容。
     * 该接口是 RAG 能力的稳定边界，调用方不应直接依赖 VectorStore / Redis / metadata 初始化细节。
     *
     * @param request 检索请求
     * @return 检索结果
     */
    SpecSearchResult search(SpecSearchRequest request);
}
