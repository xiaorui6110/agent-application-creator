package com.xiaorui.agentapplicationcreator.ai;

import cn.hutool.core.lang.UUID;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.xiaorui.agentapplicationcreator.ai.model.dto.CallAgentRequest;
import com.xiaorui.agentapplicationcreator.ai.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @description: 简易版应用生成智能体开发
 *              TODO ✨ 给 threadId 做持久化管理（Redis / MySQL）、✨ 加一个 ChatHistoryService 显示完整会话历史
 *                   ✨ 支持流式响应（SSE）做成 ChatGPT 一样的输出、✨ 全链路日志 + Agent 调用审计系统
 * @author: xiaorui
 * @date: 2025-12-10 13:07
 **/
@Slf4j
@Component
public class MiniAppCreator {

    private final static Integer MAX_INPUT_LENGTH = 2000;

    @Resource
    private ReactAgent appCreatorAgent;

    @Resource
    private UserService userService;

    /**
     * 智能体对话
     */
    public AgentResponse chat(CallAgentRequest callAgentRequest) throws GraphRunnerException {
        // 获取当前用户
        String userId = SecurityUtil.getUser().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // threadId：沿用 or 生成
        String threadId = Optional.ofNullable(callAgentRequest.getThreadId())
                .filter(StringUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());

        // TODO 第一次对话时，就应该将 userId -> threadId 存储起来， 在UserService中编写方法来存储

        // 强制绑定 userId 与 threadId
        if (!threadBelongsToUser(userId, threadId)) {
            throw new BusinessException("非法对话 ID，已阻断", ErrorCode.PARAMS_ERROR);
        }
        // 输入校验
        String callAgentRequestMessage = callAgentRequest.getMessage();
        validateUserInput(callAgentRequestMessage);
        // 构建配置
        RunnableConfig runnableConfig = buildRunnableConfig(threadId, userId);
        // 调用 Agent
        AssistantMessage response;
        try {
            response = appCreatorAgent.call(callAgentRequestMessage, runnableConfig);
        } catch (Exception e) {
            log.error("Agent 调用失败, threadId={}, userId={}, error={}", threadId, userId, e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
        }
        // 构建返回
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setThreadId(threadId);
        agentResponse.setUserId(userId);
        agentResponse.setReply(response.getText());
        agentResponse.setTimestamp(System.currentTimeMillis());
        return agentResponse;
    }

    /**
     * 判断对话是否属于当前用户（否则用户 A 可能传入用户 B 的 threadId，看到 B 的对话内容（非常危险））
     */
    private boolean threadBelongsToUser(String userId, String threadId) {
        // TODO 需要用 Redis 或 MySQL （userId -> threadId list），应该最后是要落库的，用 threadId 标识会话
        //  好比：https://chatgpt.com/c/6938df19-bb9c-8324-afd2-3257481af02e
        return true;
    }

    /**
     * 构建配置
     */
    private RunnableConfig buildRunnableConfig(String threadId, String userId) {
        return RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("user_id", userId)
                .build();
    }

    /**
     * 用户输入校验
     */
    private void validateUserInput(String input) {
        if (StringUtil.isBlank(input)) {
            throw new BusinessException("输入不能为空", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("输入过长，请分段发送", ErrorCode.PARAMS_ERROR);
        }
        // TODO 敏感词检测/安全检测/Prompt 攻击检测/超长文本截断... ，可按需扩展 https://github.com/houbb/sensitive-word
        // https://blog.csdn.net/m0_62128476/article/details/144548205
        //if (SensitiveWordUtil.contains(input)) {
        //    throw new BusinessException("输入包含不适宜内容");
        //}
    }

}
