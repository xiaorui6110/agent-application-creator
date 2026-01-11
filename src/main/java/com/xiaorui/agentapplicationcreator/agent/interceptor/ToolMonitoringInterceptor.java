package com.xiaorui.agentapplicationcreator.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;

/**
 * @description: 工具监控拦截器（拦截和修改工具调用）
 * @author: xiaorui
 * @date: 2026-01-11 11:23
 **/
public class ToolMonitoringInterceptor extends ToolInterceptor {

    @Override
    public String getName() {
        return "ToolMonitoringInterceptor";
    }

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        String toolName = request.getToolName();
        long startTime = System.currentTimeMillis();

        System.out.println("Execution Tool: " + toolName);

        try {
            ToolCallResponse response = handler.call(request);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Tool " + toolName + " Execution Successful (Time-consuming: " + duration + "ms)");

            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("Tool " + toolName + " Execution Successful (Time-consuming: " + duration + "ms): " + e.getMessage());

            return ToolCallResponse.of(
                    request.getToolCallId(),
                    request.getToolName(),
                    "Tool execution failed: " + e.getMessage()
            );
        }
    }

}