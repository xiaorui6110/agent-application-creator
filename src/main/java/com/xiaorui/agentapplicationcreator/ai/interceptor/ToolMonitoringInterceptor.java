package com.xiaorui.agentapplicationcreator.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;

/**
 * @description: TODO ToolInterceptor - 监控和错误处理（错误重试、权限检查、结果缓存、审计日志）
 *                  <a href="https://java2ai.com/docs/frameworks/agent-framework/tutorials/hooks#%E5%AE%9E%E9%99%85%E7%A4%BA%E4%BE%8B">...</a>
 * @author: xiaorui
 * @date: 2025-12-11 13:51
 **/

public class ToolMonitoringInterceptor extends ToolInterceptor {
    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        //long startTime = System.currentTimeMillis();
        //try {
        //    ToolCallResponse response = handler.call(request);
        //    logSuccess(request, System.currentTimeMillis() - startTime);
        //    return response;
        //} catch (Exception e) {
        //    logError(request, e, System.currentTimeMillis() - startTime);
        //    return ToolCallResponse.error(request.getToolCall(),
        //            "工具执行遇到问题，请稍后重试");
        //}
        return null;
    }

    @Override
    public String getName() {
        return "ToolMonitoringInterceptor";
    }
}