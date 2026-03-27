package com.xiaorui.agentapplicationcreator.agent.model.protocol;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaorui.agentapplicationcreator.agent.model.enums.AgentResponseTypeEnum;
import com.xiaorui.agentapplicationcreator.agent.model.enums.CodeGenTypeEnum;
import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.agent.model.schema.StructuredReply;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;

import java.util.Map;

/**
 * @author xiaorui
 */
public class AgentOutputProtocolResolver {

    /**
     * 解析 Agent 输出，校验是否为 合法 JSON 格式
     */
    public AgentResponse parse(String rawResponseText) {
        if (StrUtil.isBlank(rawResponseText)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Agent 输出为空");
        }
        final AgentResponse agentResponse;
        try {
            agentResponse = JSONUtil.toBean(rawResponseText, AgentResponse.class, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Agent 输出不是合法 JSON");
        }
        normalize(agentResponse);
        validate(agentResponse);
        return agentResponse;
    }

    /**
     * 正常规范化 Agent 输出
     */
    private void normalize(AgentResponse agentResponse) {
        if (agentResponse == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Agent 输出解析失败");
        }
        agentResponse.setReply(StrUtil.trim(agentResponse.getReply()));
        agentResponse.setResponseType(resolveResponseType(agentResponse));
        agentResponse.setCodeGenType(normalizeCodeGenType(agentResponse.getCodeGenType()));

        StructuredReply structuredReply = agentResponse.getStructuredReply();
        if (structuredReply == null) {
            return;
        }

        structuredReply.setType(normalizeCodeGenType(structuredReply.getType()));
        structuredReply.setGenerationMode(normalizeCodeGenType(structuredReply.getGenerationMode()));

        if (StrUtil.isBlank(structuredReply.getGenerationMode()) && StrUtil.isNotBlank(agentResponse.getCodeGenType())) {
            structuredReply.setGenerationMode(agentResponse.getCodeGenType());
        }
        if (StrUtil.isBlank(agentResponse.getCodeGenType()) && StrUtil.isNotBlank(structuredReply.getGenerationMode())) {
            agentResponse.setCodeGenType(structuredReply.getGenerationMode());
        }
        if (StrUtil.isBlank(structuredReply.getType()) && StrUtil.isNotBlank(structuredReply.getGenerationMode())) {
            structuredReply.setType(structuredReply.getGenerationMode());
        }
        if (StrUtil.isBlank(structuredReply.getGenerationMode()) && StrUtil.isNotBlank(structuredReply.getType())) {
            structuredReply.setGenerationMode(structuredReply.getType());
        }
        if (StrUtil.isBlank(structuredReply.getEntry()) && hasSingleFile(structuredReply.getFiles())) {
            structuredReply.setEntry(structuredReply.getFiles().keySet().iterator().next());
        }
    }

    /**
     * 校验 Agent 输出
     */
    private void validate(AgentResponse agentResponse) {
        require(StrUtil.isNotBlank(agentResponse.getReply()), "Agent reply 不能为空");
        require(AgentResponseTypeEnum.isValid(agentResponse.getResponseType()), "Agent responseType 不合法");

        if (StrUtil.isNotBlank(agentResponse.getCodeGenType())) {
            require(CodeGenTypeEnum.isValid(agentResponse.getCodeGenType()), "Agent codeGenType 不合法");
        }

        StructuredReply structuredReply = agentResponse.getStructuredReply();
        AgentResponseTypeEnum responseType = AgentResponseTypeEnum.valueOf(agentResponse.getResponseType());
        switch (responseType) {
            case CLARIFICATION, MODE_SELECTION, SOLUTION_DESIGN -> {
                require(structuredReply == null, "当前 responseType 不应返回 structuredReply");
                require(agentResponse.getCodeModificationPlan() == null, "当前 responseType 不应返回 codeModificationPlan");
            }
            case CODE_GENERATION -> validateCodeGeneration(agentResponse, structuredReply);
            case CODE_MODIFICATION -> require(agentResponse.getCodeModificationPlan() != null,
                    "CODE_MODIFICATION 必须返回 codeModificationPlan");
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的 Agent responseType");
        }
    }

    private void validateCodeGeneration(AgentResponse agentResponse, StructuredReply structuredReply) {
        require(StrUtil.isNotBlank(agentResponse.getCodeGenType()), "CODE_GENERATION 必须返回 codeGenType");
        require(structuredReply != null, "CODE_GENERATION 必须返回 structuredReply");
        require(hasFiles(structuredReply.getFiles()), "CODE_GENERATION 必须返回文件内容");
        require(StrUtil.isNotBlank(structuredReply.getEntry()), "CODE_GENERATION 必须返回入口文件");
        require(StrUtil.isNotBlank(structuredReply.getGenerationMode()), "CODE_GENERATION 必须返回 generationMode");
        require(agentResponse.getCodeGenType().equals(structuredReply.getGenerationMode()),
                "structuredReply.generationMode 必须与 codeGenType 一致");
    }

    private String resolveResponseType(AgentResponse agentResponse) {
        if (agentResponse.getStructuredReply() != null && hasFiles(agentResponse.getStructuredReply().getFiles())) {
            return AgentResponseTypeEnum.CODE_GENERATION.name();
        }
        if (agentResponse.getCodeModificationPlan() != null && agentResponse.getStructuredReply() == null) {
            return AgentResponseTypeEnum.CODE_MODIFICATION.name();
        }
        if (StrUtil.isNotBlank(agentResponse.getResponseType())) {
            return agentResponse.getResponseType().trim().toUpperCase();
        }
        if (StrUtil.isNotBlank(agentResponse.getCodeGenType())) {
            return AgentResponseTypeEnum.SOLUTION_DESIGN.name();
        }
        return AgentResponseTypeEnum.CLARIFICATION.name();
    }

    private String normalizeCodeGenType(String codeGenType) {
        if (StrUtil.isBlank(codeGenType)) {
            return codeGenType;
        }
        return codeGenType.trim().toLowerCase();
    }

    private boolean hasFiles(Map<String, String> files) {
        return files != null && !files.isEmpty();
    }

    private boolean hasSingleFile(Map<String, String> files) {
        return files != null && files.size() == 1;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Agent 输出协议不合法: " + message);
        }
    }
}
