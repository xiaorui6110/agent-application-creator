package com.xiaorui.agentapplicationcreator.ai.config;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.xiaorui.agentapplicationcreator.ai.hook.LoggingHook;
import com.xiaorui.agentapplicationcreator.ai.interceptor.ToolErrorInterceptor;
import com.xiaorui.agentapplicationcreator.ai.model.result.SingleFileCodeResult;
import com.xiaorui.agentapplicationcreator.ai.tool.ExampleTestTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;


/**
 * @description: agent 核心配置   TODO 一些 hook、tool、interceptor 等等还需优化实现
 * @author: xiaorui
 * @date: 2025-12-10 14:57
 **/
@Configuration
public class AppCreatorAgentConfig {

    /**
     * 系统提示词
     */
    private final String SYSTEM_PROMPT = FileUtil.readString("prompt/system_prompt.md", StandardCharsets.UTF_8);

    /**
     * 更详细的指令（主要是对 AI 的行为进行限制，提高效率）
     * TODO 可能需要根据实际测试进行调整（INSTRUCTION）（以下是用在 Cursor 上的 Rules）
     */
    private final String INSTRUCTION = FileUtil.readString("prompt/system_instruction.md",StandardCharsets.UTF_8);


    /**
     * 创建 agent
     */
    @Bean
    public ReactAgent singleFileAppCreatorAgent(ChatModel chatModel) {

        // 创建 Hooks 和 Interceptors
        LoggingHook loggingHook = new LoggingHook();
        ToolErrorInterceptor toolErrorInterceptor = new ToolErrorInterceptor();

        // 直接在这里创建工具回调，避免循环依赖
        ToolCallback exampleTestTool = FunctionToolCallback
                .builder("ExampleTestTool", new ExampleTestTool())
                .description("获取当前北京时间")
                .inputType(String.class)
                .build();

        // 使用 BeanOutputConverter 生成 outputSchema 结构化输出 TODO 之后使用系统总体通用结构化输出格式
        BeanOutputConverter<SingleFileCodeResult> outputConverter = new BeanOutputConverter<>(SingleFileCodeResult.class);
        String singleFileCodeResultFormat = outputConverter.getFormat();

        return ReactAgent.builder()
                // 模型名称（自定义）
                .name("app_creator_agent")
                // 具体模型（可选）
                .model(chatModel)
                // 系统提示词（自定义）
                .systemPrompt(SYSTEM_PROMPT)
                // 详细指令（自定义）
                //.instruction(INSTRUCTION)
                // 定义响应格式（可选）
                //.outputSchema(singleFileCodeResultFormat)
                // 工具调用（可组合使用）TODO 考虑使用 tool + CodeFileSaverUtil 让 agent 执行后自动进行代码的保存
                .tools(exampleTestTool)
                // 限制模型调用次数（可组合使用）（使用多个 Hooks 和 Interceptors 时，理解执行顺序很重要）
                .hooks(loggingHook, ModelCallLimitHook.builder().runLimit(50).build())
                // 工具错误处理（可组合使用）
                .interceptors(toolErrorInterceptor)
                // 只做短生命周期
                .saver(new MemorySaver())
                .build();
    }

}