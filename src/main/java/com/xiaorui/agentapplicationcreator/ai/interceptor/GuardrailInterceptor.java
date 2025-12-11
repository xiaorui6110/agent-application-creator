package com.xiaorui.agentapplicationcreator.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;

/**
 * @description: TODO ModelInterceptor - 内容安全检查（内容安全、动态提示、日志记录、性能监控）
 *                  <a href="https://java2ai.com/docs/frameworks/agent-framework/tutorials/hooks#%E5%AE%9E%E9%99%85%E7%A4%BA%E4%BE%8B">...</a>
 * @author: xiaorui
 * @date: 2025-12-11 13:49
 **/

public class GuardrailInterceptor extends ModelInterceptor {
    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        //// 前置：检查输入
        //if (containsSensitiveContent(request.getMessages())) {
        //    return ModelResponse.blocked("检测到不适当的内容");
        //}
        //
        //// 执行调用
        //ModelResponse response = handler.call(request);
        //
        //// 后置：检查输出
        //return sanitizeIfNeeded(response);
        return null;
    }

    @Override
    public String getName() {
        return "GuardrailInterceptor";
    }

}