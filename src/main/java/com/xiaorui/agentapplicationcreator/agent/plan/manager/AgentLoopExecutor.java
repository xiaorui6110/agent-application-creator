package com.xiaorui.agentapplicationcreator.agent.plan.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.AgentBehaviorMemory;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.CodeModificationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.result.ExecutionResult;
import com.xiaorui.agentapplicationcreator.agent.plan.service.AgentMemoryStore;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanExecutor;
import com.xiaorui.agentapplicationcreator.agent.plan.service.PlanValidator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * @description: Agent 闭环执行器（agent 反思之前的执行计划）
 * @author: xiaorui
 * @date: 2026-01-10 13:54
 **/
@Slf4j
@Component
public class AgentLoopExecutor {

    @Resource
    private AgentAppCreator agent;

    @Resource
    private PlanValidator validator;

    @Resource
    private PlanExecutor executor;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private AgentMemoryExtractor memoryExtractor;

    @Resource
    private AgentMemoryStore memoryStore;

    /**
     * 闭环执行（调用 agent 3 次）
     */
    public ExecutionResult runWithReflection(String userInput, String threadId) throws JsonProcessingException {

        SystemOutput systemOutput = agent.chatTest(userInput, threadId);
        CodeModificationPlan plan = systemOutput.getAgentResponse().getCodeModificationPlan();

        for (int round = 1; round <= 3; round++) {

            ValidatedPlan validated = validator.validate(plan);
            ExecutionResult result = executor.execute(validated);
            log.info("Agent 第 {} 轮反思", round);
            if (result.isSuccess() && result.isVerified()) {
                // 终止：成功
                return result;
            }
            // 失败 -> 进入反思模式
            String builtPlanPrompt = buildPlanPrompt(userInput);
            plan = reflectAndRepair(builtPlanPrompt, plan, result, threadId);
            // 保存行为记忆
            Optional<AgentBehaviorMemory> memory = memoryExtractor.extractMemory(builtPlanPrompt, threadId , plan, result);
            memory.ifPresent(memoryStore::saveOrUpdate);
        }
        throw new RuntimeException("Agent 在 3 轮反思后仍未完成任务");
    }

    /**
     * 反思并修复（调用了一次 agent）
     */
    private CodeModificationPlan reflectAndRepair(String userInput, CodeModificationPlan oldPlan, ExecutionResult result, String threadId)
            throws JsonProcessingException {
        String prompt = ReflectionPromptTemplates.FILE_MODIFICATION_REFLECTION
                .replace("{{userRequest}}", userInput)
                .replace("{{planJson}}", toJson(oldPlan))
                .replace("{{executionResultJson}}", toJson(result));

        SystemOutput systemOutput = agent.chatTest(prompt, threadId);
        String agentReply = systemOutput.getAgentResponse().getReply();
        return objectMapper.readValue(agentReply, CodeModificationPlan.class);
    }

    /**
     * 构建计划提示
     */
    public String buildPlanPrompt(String userInput) {

        StringBuilder memoryBlock = new StringBuilder();

        List<AgentBehaviorMemory> memories = memoryStore.getAll();
        if (!memories.isEmpty()) {
            memoryBlock.append("你过去的执行经验（必须遵守）：\n");
            for (AgentBehaviorMemory m : memories) {
                memoryBlock.append("- ")
                        .append(m.getFailurePattern())
                        .append(" → ")
                        .append(m.getCorrectionHint())
                        .append("\n");
            }
        }

        return """
                你是一个工程级代码修改 Agent。
                
                %s
                
                用户需求：
                %s
                
                请生成 CodeModificationPlan（JSON）：
                """.formatted(memoryBlock.toString(), userInput);
    }

}

