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
import com.xiaorui.agentapplicationcreator.ai.creator.AgentAppCreator;
import com.xiaorui.agentapplicationcreator.ai.model.response.SystemOutput;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * @description: agent 调用测试，参考最新官方文档 <a href="https://java2ai.com/docs/frameworks/agent-framework/tutorials/agents">...</a>
 * @author: xiaorui
 * @date: 2025-12-10 14:14
 **/
@SpringBootTest
public class AgentAppCreatorTest {

    @Resource
    private ReactAgent appCreatorAgent;

    @Resource
    private AgentAppCreator agentAppCreator;

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
     * 测试流式输出（TODO 通过 Generation 来支持流式输出，并不是正统的 Spring WebFlux 实现）
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

        String threadId = UUID.randomUUID().toString();
        SystemOutput systemOutput = agentAppCreator.chatTest("我想要做一个番茄计时专注小工具，使用原生HTML实现", threadId);
        System.out.println("Agent Response: -----------" + "\n" + systemOutput);

        /*
        结构化输出：

        SystemOutput(threadId=f804ff7e-5a59-463d-8090-fc5d520c3c66, userId=user_id_123456, messageId=f804ff7e-5a59-463d-8090-fc5d520c3c66, agentName=app-creator-agent, agentOutput={
          "confidence": 0.95,
          "intentSummary": "用户希望创建一个使用原生HTML、CSS和JavaScript实现的番茄计时专注小工具，具备基本的计时功能和交互性。",
          "metadata": {},
          "reply": "已为您生成一个基于原生 HTML、CSS 和 JavaScript 的番茄计时专注小工具。该工具支持标准的25分钟工作计时和5分钟休息计时，具有开始、暂停和重置功能，并在时间结束时弹出提示。界面简洁美观，响应式设计适配桌面和移动设备。",
          "structuredReply": {
            "description": "一个使用原生 HTML、CSS 和 JavaScript 实现的番茄计时器，支持开始、暂停、重置功能，包含工作与休息模式切换。",
            "entry": "pomodoro-timer.html",
            "files": {
              "pomodoro-timer.html": "<!DOCTYPE html>\n<html lang=\"zh\">\n<head>\n    <meta charset=\"UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n    <title>番茄计时器</title>\n    <style>\n        * {\n            margin: 0;\n            padding: 0;\n            box-sizing: border-box;\n        }\n\n        body {\n            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n            background: linear-gradient(135deg, #f6d365, #fda085);\n            height: 100vh;\n            display: flex;\n            justify-content: center;\n            align-items: center;\n            color: #333;\n        }\n\n        .container {\n            background-color: rgba(255, 255, 255, 0.92);\n            border-radius: 20px;\n            padding: 30px;\n            width: 90%;\n            max-width: 500px;\n            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);\n            text-align: center;\n        }\n\n        h1 {\n            font-size: 2rem;\n            margin-bottom: 20px;\n            color: #e74c3c;\n        }\n\n        .timer-display {\n            font-size: 4rem;\n            font-weight: bold;\n            margin: 20px 0;\n            color: #2c3e50;\n        }\n\n        .status {\n            font-size: 1.2rem;\n            margin-bottom: 20px;\n            color: #7f8c8d;\n        }\n\n        .controls button {\n            background-color: #e74c3c;\n            color: white;\n            border: none;\n            padding: 12px 24px;\n            margin: 0 10px;\n            border-radius: 50px;\n            cursor: pointer;\n            font-size: 1rem;\n            transition: background-color 0.3s ease;\n        }\n\n        .controls button:hover {\n            background-color: #c0392b;\n        }\n\n        .controls button:disabled {\n            background-color: #95a5a6;\n            cursor: not-allowed;\n        }\n\n        @media (max-width: 600px) {\n            .timer-display {\n                font-size: 3rem;\n            }\n\n            h1 {\n                font-size: 1.8rem;\n            }\n\n            .controls button {\n                padding: 10px 20px;\n                margin: 5px;\n            }\n        }\n    </style>\n</head>\n<body>\n    <div class=\"container\">\n        <h1>番茄计时器</h1>\n        <div class=\"status\" id=\"status\">专注时间</div>\n        <div class=\"timer-display\" id=\"timer\">25:00</div>\n        <div class=\"controls\">\n            <button id=\"startBtn\">开始</button>\n            <button id=\"pauseBtn\" disabled>暂停</button>\n            <button id=\"resetBtn\">重置</button>\n        </div>\n    </div>\n\n    <script>\n        // 定义时间（单位：秒）\n        const WORK_TIME = 25 * 60; // 25分钟\n        const BREAK_TIME = 5 * 60;  // 5分钟\n\n        // 获取DOM元素\n        const timerDisplay = document.getElementById('timer');\n        const statusText = document.getElementById('status');\n        const startBtn = document.getElementById('startBtn');\n        const pauseBtn = document.getElementById('pauseBtn');\n        const resetBtn = document.getElementById('resetBtn');\n\n        let timeLeft = WORK_TIME;\n        let isRunning = false;\n        let isWorkTime = true;\n        let intervalId = null;\n\n        // 格式化时间为 MM:SS\n        function formatTime(seconds) {\n            const mins = Math.floor(seconds / 60).toString().padStart(2, '0');\n            const secs = (seconds % 60).toString().padStart(2, '0');\n            return `${mins}:${secs}`;\n        }\n\n        // 更新显示\n        function updateDisplay() {\n            timerDisplay.textContent = formatTime(timeLeft);\n        }\n\n        // 开始计时\n        function startTimer() {\n            if (isRunning) return;\n            isRunning = true;\n            startBtn.disabled = true;\n            pauseBtn.disabled = false;\n\n            intervalId = setInterval(() => {\n                timeLeft--;\n                updateDisplay();\n\n                if (timeLeft <= 0) {\n                    clearInterval(intervalId);\n                    isRunning = false;\n                    // 切换模式\n                    isWorkTime = !isWorkTime;\n                    timeLeft = isWorkTime ? WORK_TIME : BREAK_TIME;\n                    statusText.textContent = isWorkTime ? '专注时间' : '休息时间';\n                    alert(isWorkTime ? '专注时间结束！现在开始休息吧！' : '休息时间结束！准备继续专注了吗？');\n                    startBtn.disabled = false;\n                    pauseBtn.disabled = true;\n                }\n            }, 1000);\n        }\n\n        // 暂停计时\n        function pauseTimer() {\n            if (!isRunning) return;\n            clearInterval(intervalId);\n            isRunning = false;\n            startBtn.disabled = false;\n            pauseBtn.disabled = true;\n        }\n\n        // 重置计时\n        function resetTimer() {\n            clearInterval(intervalId);\n            isRunning = false;\n            isWorkTime = true;\n            timeLeft = WORK_TIME;\n            updateDisplay();\n            statusText.textContent = '专注时间';\n            startBtn.disabled = false;\n            pauseBtn.disabled = true;\n        }\n\n        // 初始化\n        updateDisplay();\n\n        // 绑定事件\n        startBtn.addEventListener('click', startTimer);\n        pauseBtn.addEventListener('click', pauseTimer);\n        resetBtn.addEventListener('click', resetTimer);\n    </script>\n</body>\n</html>"
            },
            "runnable": true,
            "type": "SINGLE_FILE"
          },
          "toolCalls": []
        }, fromMemory=false, timestamp=1766561217785)

         */

    }






}
