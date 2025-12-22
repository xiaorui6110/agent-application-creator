package com.xiaorui.agentapplicationcreator.ai.creator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
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
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @description: 智能体应用生成开发
 * @author: xiaorui
 * @date: 2025-12-10 13:07
 **/
@Slf4j
@Component
public class AgentAppCreator {

    private final static Integer MAX_INPUT_LENGTH = 2000;

    private final String SINGLE_HTML_PROMPT = FileUtil.readString("prompt/front_single_html_prompt.md", StandardCharsets.UTF_8);

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
    public AgentResponse chat(String userMessage, String transThreadId) {
        // 获取当前用户
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // threadId：非空沿用旧的 / 空的生成新的  TODO 这里可以将 threadId 存到 Redis 缓存中，以及 threadId 生命周期管理
        String threadId = Optional.ofNullable(transThreadId)
                .filter(StringUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());
        // 强制绑定 userId 与 threadId
        if (!threadBelongsToUser(userId, threadId)) {
            throw new BusinessException("非法对话 ID，已阻断访问", ErrorCode.FORBIDDEN_ERROR);
        }
        // 输入校验
        validateUserInput(userMessage);
        // 保存用户输入到 MongoDB 中（底层已校验插入成功与否的错误处理，此处不做追加处理 log）
        agentChatMemoryService.saveMessage(buildMessage(userId, threadId, "user", userMessage));
        // 构建配置
        RunnableConfig runnableConfig = buildRunnableConfig(threadId, userId);
        // 调用 Agent
        AssistantMessage response;
        try {
            response = appCreatorAgent.call(userMessage, runnableConfig);
            // 保存 Agent 回复到 MongoDB 中
            agentChatMemoryService.saveMessage(buildMessage(userId, threadId, "assistant", response.getText()));
        } catch (Exception e) {
            log.error("agent call failed, threadId={}, userId={}, error={}", threadId, userId, e.getMessage(), e);
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
     * TODO 让 agent 获取历史对话信息，来维持记忆
     */

    /**
     * 智能体对话，流式输出，基于 Server-Sent Events (SSE) 协议（DashScope SDK 实现）（TODO 未解耦，暂时不使用，待优化，比如输出格式、等等）
     * <a href="https://bailian.console.aliyun.com/tab=doc?tab=doc#/doc/?type=model&url=2866129">...</a>
     */
    public AgentResponse streamChat(String userMessage, String transThreadId)
            throws NoApiKeyException, InputRequiredException, InterruptedException {
        // 获取当前用户
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // threadId：非空沿用旧的 / 空的生成新的  TODO 这里可以将 threadId 存到 Redis 缓存中，以及 threadId 生命周期管理
        String threadId = Optional.ofNullable(transThreadId)
                .filter(StringUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());
        // 强制绑定 userId 与 threadId
        if (!threadBelongsToUser(userId, threadId)) {
            throw new BusinessException("非法对话 ID，已阻断访问", ErrorCode.FORBIDDEN_ERROR);
        }
        // 输入校验
        validateUserInput(userMessage);
        // TODO 感觉还是有问题，主要就是怎么维持记忆呢，这里没有 threadId 传递给智能体（行，以下解决疑问）
        // 通义千问 API 是无状态的，不会保存对话历史。要实现多轮对话，需在每次请求中显式传入历史对话消息（好吧）
        // 保存用户输入到 MongoDB 中（底层已校验插入成功与否的错误处理，此处不做追加处理 log）
        agentChatMemoryService.saveMessage(buildMessage(userId, threadId, "user", userMessage));
        // 初始化 Generation 实例
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                // TODO 这里需要对应修改
                .content(SINGLE_HTML_PROMPT)
                .build();
        CountDownLatch latch = new CountDownLatch(1);
        // 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey("sk-b1aea9f904d6478db3fa8bf439a1a460")
                .model("qwen3-coder-plus")
                .messages(Arrays.asList(systemMsg,(Message.builder().role(Role.USER.getValue()).content(userMessage).build())))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                // 开启增量输出，流式返回
                .incrementalOutput(true)
                .build();
        // 发起流式调用并处理响应
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder fullContent = new StringBuilder();
        System.out.print("AI: ");
        result
                // IO线程执行请求
                .subscribeOn(Schedulers.io())
                // 计算线程处理响应
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onNext: 处理每个响应片段
                        message -> {
                            String content = message.getOutput().getChoices().getFirst().getMessage().getContent();
                            String finishReason = message.getOutput().getChoices().getFirst().getFinishReason();
                            // 输出内容
                            System.out.print(content);
                            fullContent.append(content);
                            // 当 finishReason 不为 null 时，表示是最后一个 chunk，输出用量信息
                            if (finishReason != null && !"null".equals(finishReason)) {
                                System.out.println("\n--- 请求用量 ---");
                                System.out.println("输入 Tokens：" + message.getUsage().getInputTokens());
                                System.out.println("输出 Tokens：" + message.getUsage().getOutputTokens());
                                System.out.println("总 Tokens：" + message.getUsage().getTotalTokens());
                            }
                            System.out.flush(); // 立即刷新输出
                        },
                        // onError: 处理错误
                        error -> {
                            System.err.println("\n请求失败: " + error.getMessage());
                            log.error("agent stream call failed, threadId={}, userId={}, error={}", threadId, userId, error.getMessage());
                            latch.countDown();
                            throw new BusinessException("AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
                        },
                        // onComplete: 完成回调
                        () -> {
                            System.out.println(); // 换行
                            // System.out.println("完整响应: " + fullContent.toString());
                            latch.countDown();
                        }
                );
        // 主线程等待异步任务完成
        latch.await();
        // 保存 Agent 回复到 MongoDB 中
        agentChatMemoryService.saveMessage(buildMessage(userId, threadId, "assistant", fullContent.toString()));
        System.out.println("程序执行完成");
        // 构建返回值
        return AgentResponse.builder()
                .threadId(threadId)
                .userId(userId)
                .messageId(UUID.randomUUID().toString())
                .agentName("app-creator-agent")
                // 使用 try/catch 后，外部代码块获取不到 AI 回复信息，故未使用 try/catch
                .reply(fullContent.toString())
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
