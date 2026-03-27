package com.xiaorui.agentapplicationcreator.controller;

import com.xiaorui.agentapplicationcreator.agent.model.dto.AgentTaskStatus;
import com.xiaorui.agentapplicationcreator.agent.model.dto.CallAgentRequest;
import com.xiaorui.agentapplicationcreator.agent.orchestrator.AgentOrchestrator;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.ratelimit.RateLimit;
import com.xiaorui.agentapplicationcreator.manager.ratelimit.RateLimitTypeEnum;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private AgentOrchestrator agentOrchestrator;

    /**
     * 智能体对话接口（每个用户在 60؜ 秒内最多只能发起 5 次智能体对话请求）
     */
    @PostMapping("/chat")
    @Operation(summary = "智能体对话接口" , description = "每个用户在 60؜ 秒内最多只能发起 5 次智能体对话请求")
    @Parameter(name = "callAgentRequest", description = "智能体对话请求参数")
    @RateLimit(limitType = RateLimitTypeEnum.USER, rate = 5, rateInterval = 60, message = "智能体对话请求过于频繁，请稍后再试")
    public ServerResponseEntity<AgentTaskStatus> chat(@RequestBody CallAgentRequest callAgentRequest) throws IOException {
        ThrowUtil.throwIf(callAgentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String message = callAgentRequest.getMessage();
        String threadId = callAgentRequest.getThreadId();
        String appId = callAgentRequest.getAppId();
        AgentTaskStatus agentTaskStatus = agentOrchestrator.handleUserMessage(message, threadId, appId);
        return ServerResponseEntity.success(agentTaskStatus);
    }

/*    *//**
     * 智能体对话接口（流式输出）要修改好多噢，暂时舍弃
     *//*
    @PostMapping("/stream_chat")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "智能体对话接口（流式输出）" , description = "流式输出")
    @Parameter(name = "callAgentRequest", description = "智能体对话请求参数")
    public ServerResponseEntity<SystemOutput> streamChat(@RequestBody CallAgentRequest callAgentRequest) throws NoApiKeyException, InputRequiredException, InterruptedException, IOException {
        ThrowUtil.throwIf(callAgentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String message = callAgentRequest.getMessage();
        String threadId = callAgentRequest.getThreadId();
        String appId = callAgentRequest.getAppId();
        SystemOutput systemOutput = agentAppCreator.streamChat(message, threadId, appId);
        CodeFileSaverUtil.writeFilesToLocal(systemOutput.getAgentResponse().getStructuredReply().getFiles(), appId);
        return ServerResponseEntity.success(systemOutput);
    }*/

}
