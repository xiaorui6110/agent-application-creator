package com.xiaorui.agentapplicationcreator.agent.subagent;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizationIssueService;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizationPatchService;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizationRunService;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.PlatformPatternService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.scheduling.annotation.Async;
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

    @Resource
    private ReactAgent codeOptimizationAgent;

    @Resource
    private CodeOptimizationRunService codeOptimizationRunService;

    @Resource
    private CodeOptimizationIssueService codeOptimizationIssueService;

    @Resource
    private CodeOptimizationPatchService codeOptimizationPatchService;

    @Resource
    private PlatformPatternService platformPatternService;

    /**
     * 异步执行代码优化
     * 1. 主 agent 生成应用代码后，就可以异步执行代码优化 agent
     * 2. 在下一次调用主 agent 生成代码时，将副 agent 的代码优化等结果添加为输入
     */
    @Async
    public void codeOptimizeAsync(CodeOptimizationInput codeOptimizationInput) {
        // 基础校验
        String input = codeOptimizationInput.toString();
        validateInput(input);
        // 提前在外部声明 POJO 变量
        AssistantMessage response;
        CodeOptimizationResult codeOptimizationResult;
        try {
            // 调用 agent
            response = codeOptimizationAgent.call(input);
            // JSON 转 Bean（主要是为了方便获取代码文件）
            codeOptimizationResult = JSONUtil.toBean(response.getText(), CodeOptimizationResult.class,true);
        } catch (Exception e) {
            log.error("agent call failed, error={}", e.getMessage(), e);
            throw new BusinessException("平台代码优化 AI 服务暂时不可用，请稍后再试", ErrorCode.SYSTEM_ERROR);
        }
        // 优化结果处理
        handleResult(codeOptimizationInput, codeOptimizationResult);

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
        // 验证字符串是否包含敏感词（目前先使用第三方框架简单实现）
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("输入包含不适宜内容", ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 处理优化结果
     */
    private void handleResult(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult) {
        // 保存平台级新模式（平台级）
        if (codeOptimizationResult.getNewPatterns() != null) {
            platformPatternService.savePlatformPattern(codeOptimizationResult.getNewPatterns().toString());
        }
        // 保存应用级审计结果（应用级关联数据）
        codeOptimizationRunService.saveCodeOptimizationRun(codeOptimizationInput, codeOptimizationResult);
        // 保存代码优化问题清单（应用级关联数据）
        codeOptimizationIssueService.saveCodeOptimizationIssue(codeOptimizationInput, codeOptimizationResult);
        // 保存代码优化修改建议（应用级关联数据）
        codeOptimizationPatchService.saveCodeOptimizationPatch(codeOptimizationInput, codeOptimizationResult);

    }


}
