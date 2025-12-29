package com.xiaorui.agentapplicationcreator.ai.config;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.xiaorui.agentapplicationcreator.ai.hook.LoggingHook;
import com.xiaorui.agentapplicationcreator.ai.interceptor.ToolErrorInterceptor;
import com.xiaorui.agentapplicationcreator.ai.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.ai.tool.ExampleTestTool;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;


/**
 * @description: agent 核心配置   TODO 一些 hook、tool、interceptor 等等还需优化实现，消息总结，对话历史操作等等
 * @author: xiaorui
 * @date: 2025-12-10 14:57
 **/
@Configuration
public class AppCreatorAgentConfig {

    @Resource
    private RedisSaver redisSaver;

    /**
     * 系统提示词 TODO 待指定优化后的文件，先将项目完整流程跑通
     */
    private final String SYSTEM_PROMPT = FileUtil.readString("prompt/system_prompt.md", StandardCharsets.UTF_8);

    /**
     * 更详细的指令（主要是对 AI 的行为进行限制，提高效率）
     * TODO 可能需要根据实际测试进行调整（INSTRUCTION）（以下是用在 Cursor 上的 Rules）
     */
    private final String INSTRUCTION = FileUtil.readString("prompt/system_instruction.md",StandardCharsets.UTF_8);

    private final String SYSTEM_PROMPT_FRONT_SINGLE_HTML = FileUtil.readString("prompt/front_single_html_prompt.md", StandardCharsets.UTF_8);


    /**
     * 创建 agent
     */
    @Bean
    public ReactAgent singleFileAppCreatorAgent(ChatModel chatModel) {

        // 创建 Hooks 和 Interceptors
        LoggingHook loggingHook = new LoggingHook();
        ToolErrorInterceptor toolErrorInterceptor = new ToolErrorInterceptor();

        // 创建消息压缩 Hook(Spring AI Alibaba 内置实现)
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(5000)
                .messagesToKeep(20)
                .build();

        // 直接在这里创建工具回调，避免循环依赖
        ToolCallback exampleTestTool = FunctionToolCallback
                .builder("ExampleTestTool", new ExampleTestTool())
                .description("获取当前北京时间")
                .inputType(String.class)
                .build();

        // 使用 BeanOutputConverter 生成 outputSchema 结构化输出
        BeanOutputConverter<AgentResponse> outputConverter = new BeanOutputConverter<>(AgentResponse.class);
        String agentResponseFormat = outputConverter.getFormat();

        return ReactAgent.builder()
                // 模型名称（自定义）
                .name("app_creator_agent")
                // 具体模型（可选）
                .model(chatModel)
                // 系统提示词（自定义） TODO 目前还是 Test
                .systemPrompt(SYSTEM_PROMPT_FRONT_SINGLE_HTML)
                // 详细指令（自定义）
                .instruction(INSTRUCTION)
                // 定义响应格式（可选）
                .outputSchema(agentResponseFormat)
                // 工具调用（可组合使用）
                .tools(exampleTestTool)
                // 限制模型调用次数（可组合使用）（使用多个 Hooks 和 Interceptors 时，理解执行顺序很重要，before_*是正的，after_*是反的）
                .hooks(loggingHook, summarizationHook, ModelCallLimitHook.builder().runLimit(50).build())
                // 工具错误处理（可组合使用）（嵌套调用（第一个拦截器包装所有其他的））
                .interceptors(toolErrorInterceptor, TodoListInterceptor.builder().build())
                // 使用 RedisSaver
                .saver(redisSaver)
                .build();
    }

}