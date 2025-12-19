package com.xiaorui.agentapplicationcreator.controller;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.xiaorui.agentapplicationcreator.ai.MiniAppCreator;
import com.xiaorui.agentapplicationcreator.ai.model.dto.CallAgentRequest;
import com.xiaorui.agentapplicationcreator.ai.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 智能体对话 控制层
 * @author: xiaorui
 * @date: 2025-12-15 15:36
 **/

@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private MiniAppCreator miniAppCreator;

    /**
     * 智能体对话接口
     */
    @PostMapping("/chat")
    public ServerResponseEntity<AgentResponse> chat(@RequestBody CallAgentRequest callAgentRequest) {
        ThrowUtil.throwIf(callAgentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String message = callAgentRequest.getMessage();
        String threadId = callAgentRequest.getThreadId();
        AgentResponse response = miniAppCreator.chat(message, threadId);
        return ServerResponseEntity.success(response);
    }


    /**
     * 智能体对话接口（流式输出）
     */
    @PostMapping("/stream_chat")
    public ServerResponseEntity<AgentResponse> streamChat(@RequestBody CallAgentRequest callAgentRequest)
            throws NoApiKeyException, InputRequiredException, InterruptedException {
        ThrowUtil.throwIf(callAgentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String message = callAgentRequest.getMessage();
        String threadId = callAgentRequest.getThreadId();
        AgentResponse response = miniAppCreator.streamChat(message, threadId);
        return ServerResponseEntity.success(response);
    }


}
