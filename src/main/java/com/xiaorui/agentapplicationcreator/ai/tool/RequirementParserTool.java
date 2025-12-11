package com.xiaorui.agentapplicationcreator.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import java.util.function.BiFunction;

/**
 * @description: 需求解析工具
 * @author: xiaorui
 * @date: 2025-12-10 13:36
 **/

public class RequirementParserTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(
            @ToolParam(description = "User natural language requirements") String requirements,
            ToolContext toolContext) {

        // 你可以在这里调用内部 DSL、规则系统、LLM 再加工等

        // TODO 1. 真实处理逻辑。。。（Tool）

        // 这里先返回结构化 JSON，供大模型后续使用

        return """
               {
                   "projectType": "auto-detected",
                   "featureList": ["解析需求", "生成代码", "自动优化"],
                   "frontend": "Vue3 + Vite",
                   "backend": "Spring Boot",
                   "db": "MySQL",
                   "desc": "AI 已将自然语言需求转换为结构化形式。"
               }
               """;
    }
}

