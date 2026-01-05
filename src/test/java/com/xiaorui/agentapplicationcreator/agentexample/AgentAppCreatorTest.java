package com.xiaorui.agentapplicationcreator.agentexample;


import cn.hutool.core.lang.UUID;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.xiaorui.agentapplicationcreator.agent.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * @description: agent 调用测试，参考最新官方文档 <a href="https://java2ai.com/docs/frameworks/agent-framework/tutorials/agents">...</a>
 * @author: xiaorui
 * @date: 2025-12-10 14:14
 **/
@Slf4j
@SpringBootTest
public class AgentAppCreatorTest {

    @Resource
    private ReactAgent appCreatorAgent;

    @Resource
    private AgentAppCreator agentAppCreator;

    @Resource
    private ChatHistoryService chatHistoryService;

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
        System.out.println("##############################################");

        // 注意我们可以使用相同的 threadId 继续对话（第二次调用）
        response = appCreatorAgent.call("谢谢你呀^^!", runnableConfig);
        System.out.println("Agent Response 2: ------------" + "\n" + response.getText());

    }

    /**
     * 测试 Redis 持久化存储
     */
    @Test
    public void testAgentMemory() throws GraphRunnerException {

        String threadId = UUID.randomUUID().toString();
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "2").build();
        AssistantMessage response = appCreatorAgent.call("我想要做一个最简单的用户管理系统，可以给出大致方案嘛", runnableConfig);
        System.out.println("Agent Response: -----------" + "\n" + response.getText());

        /*
             清理中间节点（节省 Redis 空间）  百炼 Agent 支持配置「状态清理策略」
            从 Redis 存储数据来看，单次调用出现多个 ID 是阿里云百炼 Agent 框架的「节点化执行流程」导致的正常现象。
            业务层面只需关注 logging.after 节点的最终回复，其他节点是流程追溯数据，可保留或清理。
                阿里云百炼 Agent 并非 “一次性调用 LLM”，而是将单次用户请求拆解为多个执行节点（Node） 串联执行，
                每个节点会生成独立的 ID 用于追踪状态，最终这些节点状态都会持久化到 Redis 中，表现为 “多个 ID”。
                     __START__（起始节点）→ logging.before（前置日志）→ ModelCallLimit.beforeModel（调用限流前置）
                     → model（核心LLM调用）→ ModelCallLimit.afterModel（限流后置）→ logging.after（后置日志）→ __END__（结束）
         */
    }


    /**
     * 测试 agent 调用工具使用
     */
    @Test
    public void testAgentTools() throws GraphRunnerException {


        String threadId = UUID.randomUUID().toString();
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "3").build();
        AssistantMessage response = appCreatorAgent.call("我想要获取当前的北京时间", runnableConfig);
        System.out.println("Agent Response: -----------" + "\n" + response.getText());


    }

    /**
     * 测试 agent 单 HTML 页面生成（切换 SINGLE_HTML_PROMPT）
     */
    @Test
    public void testSingleHtmlCode() throws GraphRunnerException {

        String threadId = UUID.randomUUID().toString();
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "4").build();
        AssistantMessage response = appCreatorAgent.call("我想要做一个番茄计时专注小工具", runnableConfig);
        System.out.println("Agent Response: -----------" + "\n" + response.getText());

        //System.out.println("##############################################" + "\n");
        //// 从 JSON 中输出生成的代码（在 JSON 上看代码有转义字符的原因是内容需要符合JSON 协议，JSON 是“协议”，代码是“内容”）
        //String code = JSONUtil.toBean(response.getText(), SingleFileCodeResult.class).getHtmlCode();
        //System.out.println(code);

    }

    /**
     * 测试 agent 多文件页面生成（切换 MULTI_FILE_PROMPT）
     */
    @Test
    public void testMultiFileCode() throws GraphRunnerException {

        String threadId = UUID.randomUUID().toString();
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "5").build();
        AssistantMessage response = appCreatorAgent.call("我想要做一个留言板小工具", runnableConfig);
        System.out.println("Agent Response: -----------" + "\n" + response.getText());
    }

    /**
     * 测试流式输出（通过 Generation 来支持流式输出，并不是正统的 Spring WebFlux 实现）
     * Generation: <a href="https://springdoc.cn/spring-ai/api/chatmodel.html#Generation">...</a>
     * 流式输出：<a href="https://bailian.console.aliyun.com/tab=doc?tab=doc#/doc/?type=model&url=2866129">...</a>
     */
    @Test
    public void testStreamOutputByGen() {

        // 2. 初始化 Generation 实例
        Generation gen = new Generation();
        CountDownLatch latch = new CountDownLatch(1);

        // 3. 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey("sk-b1aea9f904d6478db3fa8bf439a1a460")
                .model("qwen3-coder-plus")
                .messages(Collections.singletonList(Message.builder().role(Role.USER.getValue()).content("我想要使用原生HTML页面做一个todolist小工具").build()))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true) // 开启增量输出，流式返回
                .build();
        // 4. 发起流式调用并处理响应
        try {
            Flowable<GenerationResult> result = gen.streamCall(param);
            StringBuilder fullContent = new StringBuilder();
            System.out.print("AI: ");
            result
                    .subscribeOn(Schedulers.io()) // IO线程执行请求
                    .observeOn(Schedulers.computation()) // 计算线程处理响应
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
                                latch.countDown();
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
            System.out.println("程序执行完成");
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 测试 agent 结构化输出（需不断优化，在优化 prompt 时需要优化结构化输出）
     */
    @Test
    public void testAgentOutput() {

        SystemOutput systemOutput = agentAppCreator.chatTest("我想要做一个番茄计时专注小工具，使用原生HTML实现","threadId_121212");
        System.out.println("Agent Response: -----------" + "\n" + systemOutput);

        /*
        结构化输出：

          SystemOutput(
            threadId=threadId_121212,
            userId=user_id_123456,
            messageId=threadId_121212,
            agentName=app-creator-agent,
            agentResponse=AgentResponse(
                reply=我将为您创建一个基于原生 HTML、CSS 和 JavaScript 的番茄计时专注小工具。,
                structuredReply=StructuredReply(
                  type=null,
                  runnable=true,
                  entry=index.html,
                  files={index.html=code},
                  description=一个使用原生 HTML、CSS 和 JavaScript 实现的番茄计时专注小工具。),
                toolCalls=[],
                intentSummary=用户希望创建一个使用原生HTML、CSS和JavaScript实现的番茄计时专注小工具，具备基本的计时功能和交互性。,
                confidence=0.95,
                metadata={}),
            fromMemory=false,
            timestamp=1766633800831)

         */

    }

    /**
     * 测试写入 agent 回复代码到本地文件夹
     */
    @Test
    public void testAgentHook() throws IOException {
        String threadId = UUID.randomUUID().toString();
        SystemOutput systemOutput = agentAppCreator.chatTest("我想要做一个留言板小工具，直接使用原生HTML实现", threadId);
        CodeFileSaverUtil.writeFilesToLocal(systemOutput.getAgentResponse().getStructuredReply().getFiles(),"app_id12121212");
        System.out.println("Agent Response: -----------" + "\n" + systemOutput);

    }


    /**
     * 测试 agent 回复保存到MySQL数据库中
     */
    @Test
    public void testAgentNewMemory() {
        String threadId = UUID.randomUUID().toString();
        SystemOutput systemOutput = agentAppCreator.chatTest("我想要做一个todolist待办事件小工具，使用原生HTML实现",threadId);

        boolean result = chatHistoryService.saveChatHistory(
                systemOutput.getAppId(),
                systemOutput.getUserId(),
                systemOutput.getAgentResponse().toString(),
                "ai");
        if(result) {
            System.out.println("保存对话历史到数据库成功");
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存对话历史到数据库失败");
        }

    }

    /**
     * 测试 agent 回复从MySQL同步到Redis中
     */
    @Test
    public void testAgentNewMemoryToRedis() {

        int i = chatHistoryService.loadChatHistoryToRedis("app_id123456", 5);
        System.out.println(i);

    }

    /**
     * 测试 agent 方法工具 TODO 再测
     */
    @Test
    public void testAgentMethodTools() throws IOException {
        String dirPath = CODE_OUTPUT_ROOT_DIR + File.separator + "123456" + File.separator + "test.txt";
        String content = Files.readString(Paths.get(dirPath), StandardCharsets.UTF_8);
        System.out.println("文件读取结果：\n" + content);
        System.out.println("读取内容长度：" + content.length());
        String threadId = UUID.randomUUID().toString();
        SystemOutput systemOutput = agentAppCreator.chatTest(
                "帮我修改在 code_output 目录下创建的 123456/test.txt 文件，将其改为 abcd1234", threadId);
        System.out.println("Agent Response: -----------" + "\n" + systemOutput);
        String contentAfter = Files.readString(Paths.get(dirPath), StandardCharsets.UTF_8);
        System.out.println("文件读取结果：\n" + contentAfter);
        System.out.println("读取内容长度：" + contentAfter.length());
    }





}
