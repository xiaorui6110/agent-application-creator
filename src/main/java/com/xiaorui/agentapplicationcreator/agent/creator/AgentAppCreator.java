package com.xiaorui.agentapplicationcreator.agent.creator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
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
import com.xiaorui.agentapplicationcreator.agent.model.protocol.AgentOutputProtocolResolver;
import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.CodeModificationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.result.ExecutionResult;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanExecutor;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanValidator;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizeResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizeResultService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.monitor.MonitorContext;
import com.xiaorui.agentapplicationcreator.manager.monitor.MonitorContextHolder;
import com.xiaorui.agentapplicationcreator.manager.stream.AgentTaskStreamManager;
import com.xiaorui.agentapplicationcreator.model.entity.ModelCallLog;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.service.*;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @author xiaorui
 */
@Slf4j
@Component
public class AgentAppCreator {

    private static final Integer MAX_INPUT_LENGTH = 2000;
    private static final Integer MAX_RESPONSE_TIME = 300000;

    private final String singleHtmlPrompt = FileUtil.readString("prompt/system_prompt_v1.md", StandardCharsets.UTF_8);
    private final AgentOutputProtocolResolver agentOutputProtocolResolver = new AgentOutputProtocolResolver();

    @Resource
    private ReactAgent appCreatorAgent;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private UserThreadBindService userThreadBindService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private CodeOptimizeResultService codeOptimizeResultService;

    @Resource
    private ModelCallLogService modelCallLogService;

    @Resource
    private AgentTaskStreamManager agentTaskStreamManager;

    @Resource
    private PlanValidator validator;

    @Resource
    private PlanExecutor executor;

    public SystemOutput chat(String userMessage, String threadId, String appId) {
        return chat(userMessage, threadId, appId, null);
    }

    public SystemOutput chat(String userMessage, String threadId, String appId, String taskId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        return executeChat(userMessage, threadId, appId, userId, taskId);
    }

    public SystemOutput chatWithUserId(String userMessage, String threadId, String appId, String userId) {
        return chatWithUserId(userMessage, threadId, appId, userId, null);
    }

    public SystemOutput chatWithUserId(String userMessage, String threadId, String appId, String userId, String taskId) {
        return executeChat(userMessage, threadId, appId, userId, taskId);
    }

    public SystemOutput chatTest(String userMessage, String threadId) {
        RunnableConfig runnableConfig = buildRunnableConfig(threadId, "user_id_123456");
        AssistantMessage response;
        AgentResponse agentResponse;
        try {
            response = appCreatorAgent.call(userMessage, runnableConfig);
            agentResponse = parseAgentResponse(response.getText());
        } catch (Exception e) {
            throw new BusinessException("AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
        }
        return SystemOutput.builder()
                .agentName("app-creator-agent-test")
                .threadId("threadId_121212")
                .userId("user_id_123456")
                .appId("app_id123456")
                .agentResponse(agentResponse)
                .fromMemory(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 旧版直接调用 DashScope SDK 的流式实现，保留用于兼容测试。
     */
    public SystemOutput streamChat(String userMessage, String transThreadId, String appId)
            throws NoApiKeyException, InputRequiredException, InterruptedException {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        String threadId = Optional.ofNullable(transThreadId)
                .filter(StringUtil::isNotBlank)
                .orElse(UUID.randomUUID().toString());
        userThreadBindService.ensureThreadOwnership(userId, threadId, "app_creator_agent");
        validateUserInput(userMessage);

        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(singleHtmlPrompt)
                .build();
        CountDownLatch latch = new CountDownLatch(1);
        GenerationParam param = GenerationParam.builder()
                .apiKey("sk-b1aea9f904d6478db3fa8bf439a1a460")
                .model("qwen3-coder-plus")
                .messages(Arrays.asList(systemMsg, Message.builder().role(Role.USER.getValue()).content(userMessage).build()))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true)
                .build();
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder fullContent = new StringBuilder();
        final long streamStartTime = System.currentTimeMillis();
        final GenerationResult[] lastUsageResult = new GenerationResult[1];

        result.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(
                        message -> {
                            lastUsageResult[0] = message;
                            String content = message.getOutput().getChoices().getFirst().getMessage().getContent();
                            if (content != null) {
                                fullContent.append(content);
                            }
                        },
                        error -> {
                            modelCallLogService.record(ModelCallLog.builder()
                                    .userId(userId)
                                    .appId(appId)
                                    .threadId(threadId)
                                    .agentName("app_creator_agent")
                                    .provider("dashscope")
                                    .modelName("qwen3-coder-plus")
                                    .callType("STREAM")
                                    .callStatus("FAILED")
                                    .latencyMs(System.currentTimeMillis() - streamStartTime)
                                    .errorMessage(error.getMessage())
                                    .build());
                            log.error("agent stream call failed, threadId={}, userId={}, error={}", threadId, userId, error.getMessage());
                            latch.countDown();
                        },
                        () -> {
                            GenerationResult finalResult = lastUsageResult[0];
                            if (finalResult != null && finalResult.getUsage() != null) {
                                modelCallLogService.record(ModelCallLog.builder()
                                        .userId(userId)
                                        .appId(appId)
                                        .threadId(threadId)
                                        .agentName("app_creator_agent")
                                        .provider("dashscope")
                                        .modelName("qwen3-coder-plus")
                                        .callType("STREAM")
                                        .callStatus("SUCCESS")
                                        .promptTokens(finalResult.getUsage().getInputTokens())
                                        .completionTokens(finalResult.getUsage().getOutputTokens())
                                        .totalTokens(finalResult.getUsage().getTotalTokens())
                                        .latencyMs(System.currentTimeMillis() - streamStartTime)
                                        .build());
                            }
                            latch.countDown();
                        }
                );

        latch.await();
        AgentResponse agentResponse = parseAgentResponse(fullContent.toString());
        return SystemOutput.builder()
                .threadId(threadId)
                .userId(userId)
                .appId(appId)
                .agentName("app-creator-agent")
                .agentResponse(agentResponse)
                .fromMemory(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 执行聊天
     */
    private SystemOutput executeChat(String userMessage, String threadId, String appId, String userId, String taskId) {
        // 如果有，拼接代码优化结果
        CodeOptimizeResult codeOptimizeResult = codeOptimizeResultService.getByAppId(appId);
        String codeOptimizeResultStr = codeOptimizeResult == null ? "" : codeOptimizeResult.toString();
        String finalInputMessage = userMessage + "\n【应用ID】" + appId + "\n【应用代码优化结果详情】：" + codeOptimizeResultStr;
        RunnableConfig runnableConfig = buildRunnableConfig(threadId, userId);
        // 设置监控上下文
        MonitorContextHolder.setContext(MonitorContext.builder()
                .userId(userId)
                .appId(appId)
                .threadId(threadId)
                .agentName("app_creator_agent")
                .build());
        // 在外部声明对象
        AssistantMessage response;
        AgentResponse agentResponse;
        try {
            publishProgress(taskId, threadId, appId, "RUNNING", "已提交模型，正在等待结构化结果");
            // 统计模型响应时间
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 调用模型
            response = appCreatorAgent.call(finalInputMessage, runnableConfig);
            stopWatch.stop();
            log.info("agent.call() execution completed, time taken = {} ms", stopWatch.getTotalTimeMillis());
            if (stopWatch.getTotalTimeMillis() > MAX_RESPONSE_TIME) {
                throw new BusinessException("智能体应用生成 AI 服务响应超时，请稍后再试", ErrorCode.SYSTEM_ERROR);
            }
            publishProgress(taskId, threadId, appId, "RUNNING", "模型响应已返回，正在解析结构化结果");
            agentResponse = parseAgentResponse(response.getText());
        } catch (Exception e) {
            // 发生错误也要清除监控上下文
            MonitorContextHolder.clearContext();
            log.error("agent call failed, threadId={}, userId={}, error={}", threadId, userId, e.getMessage(), e);
            throw new BusinessException("智能体应用生成 AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
        }
        // 清除监控上下文
        MonitorContextHolder.clearContext();
        // 保存聊天记录、异步更新应用名称、更新代码生成类型
        chatHistoryService.saveChatHistory(appId, userId, response.getText(), "ai");
        appService.updateAppNameAsync(appId, agentResponse.getAppName());
        appService.updateAppCodeGenTypeAsync(appId, agentResponse.getCodeGenType());
        publishProgress(taskId, threadId, appId, "RUNNING", "结构化结果解析完成，正在整理应用信息");
        // 校验代码修改计划
        handleCodeModificationPlan(agentResponse);

        return SystemOutput.builder()
                .threadId(threadId)
                .userId(userId)
                .appId(appId)
                .agentName("app-creator-agent")
                .agentResponse(agentResponse)
                .fromMemory(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private RunnableConfig buildRunnableConfig(String threadId, String userId) {
        return RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("user_id", userId)
                .build();
    }

    private AgentResponse parseAgentResponse(String rawResponseText) {
        return agentOutputProtocolResolver.parse(rawResponseText);
    }

    private void handleCodeModificationPlan(AgentResponse agentResponse) {
        if (agentResponse.getCodeModificationPlan() == null) {
            return;
        }
        CodeModificationPlan plan = agentResponse.getCodeModificationPlan();
        ValidatedPlan validatedPlan = validator.validate(plan);
        ExecutionResult result = executor.execute(validatedPlan);
        ThrowUtil.throwIf(!result.isVerified(), ErrorCode.SYSTEM_ERROR, "文件操作验证未通过: " + result.getErrorMessage());
        ThrowUtil.throwIf(!result.isSuccess(), ErrorCode.SYSTEM_ERROR, "文件操作执行失败: " + result.getErrorMessage());
    }

    private void validateUserInput(String input) {
        if (StringUtil.isBlank(input)) {
            throw new BusinessException("输入不能为空", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("输入过长，请分段发送", ErrorCode.PARAMS_ERROR);
        }
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("输入包含不适宜内容", ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 发布进度（主要是在实现SSE流式输出时，代码相关的实现，目前并未使用流式输出，故只是占位）
     */
    private void publishProgress(String taskId, String threadId, String appId, String taskStatus, String progressMessage) {
        if (StringUtil.isBlank(taskId)) {
            return;
        }
        agentTaskStreamManager.publishProgress(taskId, threadId, appId, taskStatus, progressMessage, "app_creator_agent");
    }
}
