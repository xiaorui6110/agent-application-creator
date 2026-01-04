package com.xiaorui.agentapplicationcreator.controller;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.dto.CallAgentRequest;
import com.xiaorui.agentapplicationcreator.agent.model.response.SystemOutput;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @description: 智能体对话 控制层（整合应用模块）
 * @author: xiaorui
 * @date: 2025-12-15 15:36
 **/

@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private AgentAppCreator agentAppCreator;


    /**
     * 智能体对话接口 TODO 多轮对话时可能还会有些问题，比如频繁写入文件、多轮对话输出格式。。。
     */
    @PostMapping("/chat")
    public ServerResponseEntity<SystemOutput> chat(@RequestBody CallAgentRequest callAgentRequest) throws IOException {
        ThrowUtil.throwIf(callAgentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String message = callAgentRequest.getMessage();
        String threadId = callAgentRequest.getThreadId();
        String appId = callAgentRequest.getAppId();
        SystemOutput systemOutput = agentAppCreator.chat(message, threadId, appId);
        CodeFileSaverUtil.writeFilesToLocal(systemOutput.getAgentResponse().getStructuredReply().getFiles(), appId);
        return ServerResponseEntity.success(systemOutput);
    }


    /**
     * 智能体对话接口（流式输出）
     */
    @PostMapping("/stream_chat")
    public ServerResponseEntity<SystemOutput> streamChat(@RequestBody CallAgentRequest callAgentRequest) throws NoApiKeyException, InputRequiredException, InterruptedException, IOException {
        ThrowUtil.throwIf(callAgentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String message = callAgentRequest.getMessage();
        String threadId = callAgentRequest.getThreadId();
        String appId = callAgentRequest.getAppId();
        SystemOutput systemOutput = agentAppCreator.streamChat(message, threadId, appId);
        CodeFileSaverUtil.writeFilesToLocal(systemOutput.getAgentResponse().getStructuredReply().getFiles(), appId);
        return ServerResponseEntity.success(systemOutput);
    }


}
