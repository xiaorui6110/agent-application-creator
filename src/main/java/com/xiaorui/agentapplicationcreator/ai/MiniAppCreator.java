package com.xiaorui.agentapplicationcreator.ai;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.xiaorui.agentapplicationcreator.ai.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.model.entity.AgentChatMessage;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.service.AgentChatMemoryService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.service.UserThreadBindService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @description: 简易版应用生成智能体开发
 *              TODO ✨ 支持流式响应（SSE）做成 ChatGPT 一样的输出、✨ 全链路日志 + Agent 调用审计系统
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

    @Resource
    private UserThreadBindService userThreadBindService;

    @Resource
    private AgentChatMemoryService agentChatMemoryService;


    /**
     * 智能体对话
     */
    public AgentResponse chat(String message, String transThreadId) throws GraphRunnerException {
        // 获取当前用户
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // threadId：非空沿用旧的 / 空的生成新的  TODO 这里可以将 threadId 存到 Redis 缓存中，threadId 生命周期管理
        String threadId = Optional.ofNullable(transThreadId)
                .filter(StringUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());
        // 强制绑定 userId 与 threadId
        if (!threadBelongsToUser(userId, threadId)) {
            throw new BusinessException("非法对话 ID，已阻断访问", ErrorCode.FORBIDDEN_ERROR);
        }
        // 输入校验
        validateUserInput(message);
        // 保存用户输入到 MongoDB 中
        agentChatMemoryService.saveMessage(buildMessage(userId, threadId, "user", message));
        // 构建配置
        RunnableConfig runnableConfig = buildRunnableConfig(threadId, userId);
        // 调用 Agent
        AssistantMessage response;
        try {
            response = appCreatorAgent.call(message, runnableConfig);
            // 保存 Agent 回复到 MongoDB 中
            agentChatMemoryService.saveMessage(buildMessage(userId, threadId, "assistant", response.getText()));
        } catch (Exception e) {
            log.error("Agent 调用失败, threadId={}, userId={}, error={}", threadId, userId, e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
        }
        // 构建返回值
        return AgentResponse.builder()
                .threadId(threadId)
                .userId(userId)
                .messageId(UUID.randomUUID().toString())
                .agentName("app-creator-agent")
                .reply(response.getText())
                .fromMemory(false)
                .timestamp(System.currentTimeMillis())
                .confidence(0.85)
                .build();
    }

    /**
     * 判断对话是否属于当前用户（否则用户 A 可能传入用户 B 的 threadId，看到 B 的对话内容，非常危险）
     */
    private boolean threadBelongsToUser(String userId, String threadId) {
        // threadId 肯定非空，新 thread：绑定
        if (StrUtil.isNotBlank(threadId)) {
            userThreadBindService.bindThread(userId, threadId, "app_creator_agent");
        } else {
            // 老 thread：校验归属
            if (!userThreadBindService.validateThreadOwner(userId, threadId)) {
                throw new BusinessException("非法对话 ID，已阻断访问", ErrorCode.FORBIDDEN_ERROR);
            }
        }
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
     * 用户输入校验：敏感词检测 ...
     */
    private void validateUserInput(String input) {
        if (StringUtil.isBlank(input)) {
            throw new BusinessException("输入不能为空", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("输入过长，请分段发送", ErrorCode.PARAMS_ERROR);
        }
        // 验证字符串是否包含敏感词（目前先使用第三方框架简单实现）
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("输入包含不适宜内容", ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 构建消息实体（MongoDB）
     */
    private AgentChatMessage buildMessage(
            String userId,
            String threadId,
            String role,
            String content) {
        AgentChatMessage msg = new AgentChatMessage();
        msg.setUserId(userId);
        msg.setThreadId(threadId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setAgentName("app_creator_agent");
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }


}
