package com.xiaorui.agentapplicationcreator.agent.model.response;

import com.xiaorui.agentapplicationcreator.agent.model.schema.StructuredReply;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.CodeModificationPlan;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description: 智能体结构化输出回复（经过 GPT 优化后）
 * @author: xiaorui
 * @date: 2025-12-12 15:00
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    /**
     * Agent 当前返回所属阶段 / 输出类型
     * 允许值：CLARIFICATION / MODE_SELECTION / SOLUTION_DESIGN / CODE_GENERATION / CODE_MODIFICATION
     */
    @Schema(description = "Agent 当前返回所属阶段 / 输出类型")
    private String responseType;

    /**
     * Agent 最终给用户的自然语言回复
     * ⚠️ 严禁包含代码
     */
    @Schema(description = "Agent 最终给用户的自然语言回复")
    private String reply;

    /**
     * 结构化回复（机器可消费）
     * ⚠️ 所有代码只能出现在这里
     */
    @Schema(description = "结构化回复")
    private StructuredReply structuredReply;

    /**
     * Agent 在本次对话中调用的工具信息
     */
    @Schema(description = "Agent 在本次对话中调用的工具信息")
    private List<ToolCallResponse> toolCalls;

    /**
     * Agent 执行的代码修改计划（根据用户提示判断生成）
     */
    @Schema(description = "Agent 执行的代码修改计划")
    private CodeModificationPlan codeModificationPlan;

    /**
     * 主 Agent 生成的应用信息，副 Agent 执行的代码优化输入（根据用户提示判断生成）
     */
    @Schema(description = "主 Agent 生成的应用信息，副 Agent 执行的代码优化输入")
    private CodeOptimizationInput codeOptimizationInput;

    /**
     * Agent 生成的应用名称（根据用户提示判断生成）
     */
    @Schema(description = "Agent 生成的应用名称")
    private String appName;

    /**
     * Agent 根据用户提示判断的应用代码生成类型（根据用户提示判断生成）
     * ⚠️ codeGenType 仅允许小写 single_file/multi_file/vue_project
     */
    @Schema(description = "Agent 根据用户提示判断的代码生成类型")
    private String codeGenType;

    /**
     * Agent 对用户意图的理解摘要（非 Chain-of-Thought）
     */
    @Schema(description = "Agent 对用户意图的理解摘要")
    private String intentSummary;

    /**
     * Agent 自评置信度（0 ~ 1）
     */
    @Schema(description = "Agent 自评置信度")
    private Double confidence;

    /**
     * 扩展元数据（调试 / 灰度 / 实验）
     */
    @Schema(description = "扩展元数据")
    private Map<String, Object> metadata;
}

