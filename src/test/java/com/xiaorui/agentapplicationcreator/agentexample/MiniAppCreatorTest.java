package com.xiaorui.agentapplicationcreator.agentexample;


import cn.hutool.core.lang.UUID;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description: agent 调用测试，参考最新官方文档 <a href="https://java2ai.com/docs/frameworks/agent-framework/tutorials/agents">...</a>
 * @author: xiaorui
 * @date: 2025-12-10 14:14
 **/
@SpringBootTest
public class MiniAppCreatorTest {

    @Resource
    private ReactAgent appCreatorAgent;

    /**
     *  测试 agent 调用
     */
    @Test
    public void testAppCreatorAgent() throws GraphRunnerException {

        // 生成一个随机的 threadId
        String threadId = UUID.randomUUID().toString();

        // threadId 是给定对话的唯一标识符，使用 threadId 维护对话上下文
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "1").build();

        // 调用 Agent（第一次调用）
        AssistantMessage response = appCreatorAgent.call("帮我做一个简易的在线图书管理系统，给出初步的设计方案吧", runnableConfig);

        // 打印结果（方便调试）
        System.out.println("Agent Response 1: ------------" + "\n" + response.getText());

        // 注意我们可以使用相同的 threadId 继续对话（第二次调用）
        response = appCreatorAgent.call("谢谢你呀^^!", runnableConfig);
        System.out.println("Agent Response 2: ------------" + "\n" + response.getText());

    }

    /**
     * 测试 Redis 持久化存储
     */
    @Test
    public void testAgentMemory() throws GraphRunnerException {

        // 生成一个随机的 threadId
        String threadId = UUID.randomUUID().toString();

        // threadId 是给定对话的唯一标识符，使用 threadId 维护对话上下文
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "2").build();

        // 调用 Agent（第一次调用）
        AssistantMessage response = appCreatorAgent.call("我想要做一个最简单的用户管理系统，可以给出大致方案嘛", runnableConfig);

        // 打印结果（方便调试）
        System.out.println("Agent Response: -----------" + "\n" + response.getText());

        /*
            TODO 清理中间节点（节省 Redis 空间）  百炼 Agent 支持配置「状态清理策略」
            从 Redis 存储数据来看，单次调用出现多个 ID 是阿里云百炼 Agent 框架的「节点化执行流程」导致的正常现象。
            业务层面只需关注 logging.after 节点的最终回复，其他节点是流程追溯数据，可保留或清理。
                阿里云百炼 Agent 并非 “一次性调用 LLM”，而是将单次用户请求拆解为多个执行节点（Node） 串联执行，
                每个节点会生成独立的 ID 用于追踪状态，最终这些节点状态都会持久化到 Redis 中，表现为 “多个 ID”。
                     __START__（起始节点）→ logging.before（前置日志）→ ModelCallLimit.beforeModel（调用限流前置）
                     → model（核心LLM调用）→ ModelCallLimit.afterModel（限流后置）→ logging.after（后置日志）→ __END__（结束）
         */
    }
}
