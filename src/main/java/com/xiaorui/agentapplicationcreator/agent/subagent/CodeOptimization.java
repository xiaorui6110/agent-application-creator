package com.xiaorui.agentapplicationcreator.agent.subagent;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.xiaorui.agentapplicationcreator.manager.monitor.MonitorContext;
import com.xiaorui.agentapplicationcreator.manager.monitor.MonitorContextHolder;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizeResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizeResultService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Component;

/**
 * @description: 智能体代码优化
 * @author: xiaorui
 * @date: 2026-01-14 10:48
 **/
@Slf4j
@Component
public class CodeOptimization {

    private final static Integer MAX_INPUT_LENGTH = 2000;

    private final static Integer MAX_RESPONSE_TIME = 60000;

    @Resource
    private ReactAgent codeOptimizationAgent;

    @Resource
    private CodeOptimizeResultService codeOptimizeResultService;

    /**
     * 异步执行代码优化
     * 1. 主 agent 生成应用代码后，就可以异步执行代码优化 agent
     * 2. 在下一次调用主 agent 生成代码时，将副 agent 的代码优化等结果添加为输入
     */
    public void codeOptimizeAsync(CodeOptimizationInput codeOptimizationInput, String userId) {
        log.info("Starting code optimization, appId={}", codeOptimizationInput.getAppId());
        // 基础校验
        validateInput(codeOptimizationInput);
        String input = codeOptimizationInput.toString();
        validateInput(input);
        // 提前在外部声明 POJO 变量
        AssistantMessage response;
        CodeOptimizationResult codeOptimizationResult;
        try {
            // 构建 RunnableConfig，传递 userId 和 threadId
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .addMetadata("user_id", userId)
                    .threadId(userId + "_code_opt_" + System.currentTimeMillis())
                    .build();

            // 使用 Future 设置超时
            java.util.concurrent.Future<AssistantMessage> future = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    MonitorContextHolder.setContext(MonitorContext.builder()
                            .userId(userId)
                            .appId(codeOptimizationInput.getAppId())
                            .threadId(runnableConfig.threadId().get())
                            .agentName("code_optimization_agent")
                            .build());
                    return codeOptimizationAgent.call(input, runnableConfig);
                } catch (GraphRunnerException e) {
                    throw new RuntimeException(e);
                } finally {
                    MonitorContextHolder.clearContext();
                }
            });

            // 调用 agent，传入 runnableConfig，带超时控制
            response = future.get(MAX_RESPONSE_TIME, java.util.concurrent.TimeUnit.MILLISECONDS);
            log.info("code optimization agent call success, appId={}", codeOptimizationInput.getAppId());
            // JSON 转 Bean（主要是为了方便获取代码文件）
            codeOptimizationResult = JSONUtil.toBean(response.getText(), CodeOptimizationResult.class,true);
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("code optimization agent timeout, appId={}", codeOptimizationInput.getAppId(), e);
            throw new BusinessException("代码优化超时，请稍后再试", ErrorCode.SYSTEM_ERROR);
        } catch (Exception e) {
            log.error("code optimization agent call failed, error={}, appId={}", e.getMessage(), codeOptimizationInput.getAppId(), e);
            throw new BusinessException("平台代码优化 AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
        }
        // 优化结果处理
        handleResult(codeOptimizationInput, codeOptimizationResult);
        log.info("Code optimization completed, appId={}", codeOptimizationInput.getAppId());
    }

    /**
     * 输入校验
     */
    private void validateInput(String input) {
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

    private void validateInput(CodeOptimizationInput input) {
        ThrowUtil.throwIf(input == null, ErrorCode.PARAMS_ERROR, "代码优化输入不能为空");
        ThrowUtil.throwIf(StringUtil.isBlank(input.getAppId()), ErrorCode.PARAMS_ERROR, "代码优化 appId 不能为空");
        ThrowUtil.throwIf(StringUtil.isBlank(input.getAppGoal()), ErrorCode.PARAMS_ERROR, "代码优化 appGoal 不能为空");
        ThrowUtil.throwIf(input.getFiles() == null || input.getFiles().isEmpty(), ErrorCode.PARAMS_ERROR, "代码优化 files 不能为空");
        // 验证字符串是否包含敏感词（目前先使用第三方框架简单实现）
    }

    /**
     * 处理优化结果（持久化）
     */
    private void handleResult(CodeOptimizationInput codeOptimizationInput, @NotNull CodeOptimizationResult codeOptimizationResult) {
        ThrowUtil.throwIf(codeOptimizationResult.getNewPatterns() == null, ErrorCode.SYSTEM_ERROR, "代码优化结果为空");
        CodeOptimizeResult codeOptimizeResult = new CodeOptimizeResult();
        codeOptimizeResult.setAppId(codeOptimizationInput.getAppId());
        codeOptimizeResult.setCodeOptimizeSummary(codeOptimizationResult.getSummary());
        codeOptimizeResult.setCodeOptimizeIssues(codeOptimizationResult.getIssues().toString());
        codeOptimizeResult.setCodeOptimizeSuggestions(codeOptimizationResult.getSuggestedDiff().toString());
        codeOptimizeResult.setPlatformExperience(codeOptimizationResult.getNewPatterns().toString());
        codeOptimizeResult.setAgentConfidence(codeOptimizationResult.getConfidence());
        boolean saved = codeOptimizeResultService.save(codeOptimizeResult);
        if (!saved) {
            log.error("save code optimize result failed");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存代码优化结果失败");
        }
    }


}
