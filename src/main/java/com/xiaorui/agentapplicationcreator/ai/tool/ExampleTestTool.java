package com.xiaorui.agentapplicationcreator.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.BiFunction;

/**
 * @description: 测试用
 * @author: xiaorui
 * @date: 2025-12-12 14:14
 **/
public class ExampleTestTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(
            @ToolParam(description = "ExampleTestTool") String code,
            ToolContext toolContext) {

        return """
               {
                   "summary": "this is a test tool, no usage",
                   "suggestions": [
                       "test ok ..."
                   ]
               }
               """;
    }
}
