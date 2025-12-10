package com.xiaorui.agentapplicationcreator.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.BiFunction;

/**
 * @description: 项目生成工具
 * @author: xiaorui
 * @date: 2025-12-10 13:37
 **/

public class ProjectGeneratorTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(
            @ToolParam(description = "Structured project requirements JSON") String json,
            ToolContext toolContext) {

        // 可接入你自己的代码生成器
        // AI 会调用此工具并继续编写代码文件

        String userId = (String) toolContext.getContext().get("user_id");

        return """
               {
                 "msg": "Project generated successfully",
                 "user": "%s",
                 "output": "/tmp/generated-project-%s.zip"
               }
               """.formatted(userId, System.currentTimeMillis());
    }
}
