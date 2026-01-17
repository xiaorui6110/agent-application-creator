package com.xiaorui.agentapplicationcreator.agent.plan.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.AgentBehaviorMemory;
import jakarta.annotation.Resource;

import java.time.Instant;
import java.util.Optional;

/**
 * @description: Agent 行为记忆提取器（弃用，后续可以完善，但没太大必要）
 * @author: xiaorui
 * @date: 2026-01-10 17:10
 **/
//@Component
public class AgentMemoryExtractor {

    @Resource
    private AgentAppCreator agent;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 提取 Agent 行为记忆
     */
    public Optional<AgentBehaviorMemory> extractMemory(String userRequest, String threadId, Object plan, Object executionResult) {
        try {
            String prompt = ReflectionPromptTemplates.EXTRACT_MEMORY
                    .replace("{{userRequest}}", userRequest)
                    .replace("{{planJson}}", objectMapper.writeValueAsString(plan))
                    .replace("{{executionResultJson}}", objectMapper.writeValueAsString(executionResult));

            //  这里只能使用多 agent 方式，单个的 prompt 无法重用!!!  这里先这样，那后面肯定要多智能体了
            SystemOutput systemOutput = agent.chatTest(prompt, threadId);
            String resp = systemOutput.getAgentResponse().getReply();

            if ("NONE".equalsIgnoreCase(resp)) {
                return Optional.empty();
            }
            //  这后面要改成 AgentBehaviorMemory 对象，多 agent
            JsonNode node = objectMapper.readTree(resp);
            return Optional.of(
                    AgentBehaviorMemory.builder()
                            .failurePattern(node.get("failurePattern").asText())
                            .consequence(node.get("consequence").asText())
                            .correctionHint(node.get("correctionHint").asText())
                            .frequency(1)
                            .lastSeen(Instant.now())
                            .build()
            );
        } catch (Exception e) {
            // memory 不能阻塞主流程
            return Optional.empty();
        }
    }
}
