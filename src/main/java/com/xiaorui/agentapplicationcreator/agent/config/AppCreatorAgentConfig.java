package com.xiaorui.agentapplicationcreator.agent.config;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolretry.ToolRetryInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolselection.ToolSelectionInterceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.xiaorui.agentapplicationcreator.agent.hook.LoggingHook;
import com.xiaorui.agentapplicationcreator.agent.hook.MessageTrimmingHook;
import com.xiaorui.agentapplicationcreator.agent.interceptor.ToolErrorInterceptor;
import com.xiaorui.agentapplicationcreator.agent.interceptor.ToolMonitoringInterceptor;
import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.agent.tool.ExampleTestTool;
import com.xiaorui.agentapplicationcreator.agent.tool.FileOperationTool;
import com.xiaorui.agentapplicationcreator.agent.tool.VerifyFileTool;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;


/**
 * @description: agent 核心配置
 * @author: xiaorui
 * @date: 2025-12-10 14:57
 **/
@Configuration
public class AppCreatorAgentConfig {

    @Resource
    private RedisSaver redisSaver;

    /**
     * 系统提示词
     */
    private final String SYSTEM_PROMPT = FileUtil.readString("prompt/optimized_system_prompt.md", StandardCharsets.UTF_8);

    /**
     * 更详细的指令（主要是对 AI 的行为进行限制，提高效率）
     * 可能需要根据实际测试进行调整（INSTRUCTION）（以下是用在 Cursor 上的 Rules）
     */
    private final String INSTRUCTION = FileUtil.readString("prompt/system_instruction.md",StandardCharsets.UTF_8);


    /**
     * 创建 agent
     */
    @Bean
    public ReactAgent appCreatorAgent(ChatModel chatModel) {

        // ========== 创建 Hooks ==========
        LoggingHook loggingHook = new LoggingHook();
        MessageTrimmingHook messageTrimmingHook = new MessageTrimmingHook();
        // 消息压缩总结 (Spring AI Alibaba 内置实现）
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(5000)
                .messagesToKeep(10)
                .build();
        // 限制模型调用次数为 50 次
        ModelCallLimitHook modelCallLimitHook = ModelCallLimitHook.builder().runLimit(50).build();

        // ========== 创建 Interceptors ==========
        ToolErrorInterceptor toolErrorInterceptor = new ToolErrorInterceptor();
        ToolMonitoringInterceptor toolMonitoringInterceptor = new ToolMonitoringInterceptor();
        // 工具重试
        ToolRetryInterceptor toolRetryInterceptor = ToolRetryInterceptor.builder().
                maxRetries(3).onFailure(ToolRetryInterceptor.OnFailureBehavior.RETURN_MESSAGE).build();
        // 在执行工具之前强制执行一个规划步骤，以概述 Agent 将要采取的步骤
        TodoListInterceptor todoListInterceptor = TodoListInterceptor.builder().build();
        // 使用一个 LLM 来决定在多个可用工具之间选择哪个工具
        ToolSelectionInterceptor toolSelectionInterceptor = ToolSelectionInterceptor.builder().build();

        // ========== 创建 Tools ==========
        // 在这里创建直接工具回调，避免循环依赖
        ToolCallback exampleTestTool = FunctionToolCallback
                .builder("ExampleTestTool", new ExampleTestTool())
                .description("获取当前北京时间")
                .inputType(String.class)
                .build();
        // 创建带有 @Tool 注解的方法工具对象
        FileOperationTool fileOperationTool = new FileOperationTool();
        VerifyFileTool verifyFileTool = new VerifyFileTool();

        // ========== 结构化输出 ==========
        // 使用 BeanOutputConverter 生成 outputSchema 结构化输出
        BeanOutputConverter<AgentResponse> outputConverter = new BeanOutputConverter<>(AgentResponse.class);
        String agentResponseFormat = outputConverter.getFormat();

        // ========== 创建 Agent ==========
        return ReactAgent.builder()
                // 模型名称
                .name("app_creator_agent")
                // 具体模型
                .model(chatModel)
                // 系统提示词（
                .systemPrompt(SYSTEM_PROMPT)
                // 详细指令
                //.instruction(INSTRUCTION)
                // 定义响应格式
                .outputSchema(agentResponseFormat)
                // 工具调用
                //.tools(exampleTestTool)
                //.methodTools(fileOperationTool, verifyFileTool)
                // 钩子调用（使用多个 Hooks 时，理解执行顺序很重要，before_* 是正的，after_* 是反的）
                .hooks(loggingHook, messageTrimmingHook, summarizationHook, modelCallLimitHook)
                // 拦截器调用（嵌套调用，第一个拦截器包装所有其他的）
                .interceptors(todoListInterceptor, toolSelectionInterceptor,
                        toolMonitoringInterceptor, toolErrorInterceptor, toolRetryInterceptor)
                // 记忆存储
                .saver(redisSaver)
                .build();
    }


    private final String CODE_PLAN_PROPMT = """
            
            你是一个【代码修改规划器】（Code Modification Planner）。

            你的职责不是修改文件，也不是调用任何工具，
            而是【生成一个结构化的代码修改计划】。
            
            你必须严格遵守以下规则：
            
            一、职责边界
            1. 你【不能】直接操作文件系统
            2. 你【不能】假设任何修改已经发生
            3. 你【不能】编造或调用任何不存在的工具
            4. 文件修改将由系统执行，你只负责规划
            
            二、输出要求
            1. 你【只能】输出 JSON
            2. 不允许输出解释性文字、注释、Markdown
            3. 输出必须符合 CodeModificationPlan 结构
            4. 所有路径必须是 code_output 目录下的相对路径
            
            三、修改规划规则
            1. 每一个文件修改必须包含 expected 条件
            2. expected 表示你对“当前文件状态”的假设
            3. 如果你无法确定 expected 内容，你必须拒绝生成计划
            4. 不允许“尝试一下”“如果失败再说”之类的计划
            
            四、安全约束
            1. 不允许访问 code_output 以外的路径
            2. 单次计划最多允许 5 个操作
            3. 不允许删除整个目录或进行模糊匹配
            
            五、失败策略
            如果用户的需求不明确，或你无法确定文件的当前状态，
            你必须输出一个空的 operations 数组，并说明 reason 字段。

            【重要】CodeModificationPlan 输出示例：
            
            {
              "planType": "CODE_MODIFICATION",
              "rootDir": "code_output",
              "operations": [
                {
                  "operationType": "OVERWRITE_FILE",
                  "path": "123456/test.txt",
                  "expected": {
                    "type": "CONTENT_EQUALS",
                    "value": "file_origin_content"
                  },
                  "content": "file_cover_content"
                }
              ],
              "verification": {
                "type": "CONTENT_EQUALS",
                "path": "123456/test.txt",
                "value": "file_cover_content"
              }
            }
                        
            现在，请根据用户输入生成 CodeModificationPlan（ planType 固定为 CODE_MODIFICATION ）。
              
            """;



}