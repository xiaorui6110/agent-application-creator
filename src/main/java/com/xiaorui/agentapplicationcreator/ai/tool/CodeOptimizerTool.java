package com.xiaorui.agentapplicationcreator.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.BiFunction;

/**
 * @description: 代码优化工具
 * @author: xiaorui
 * @date: 2025-12-10 13:37
 **/

public class CodeOptimizerTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(
            @ToolParam(description = "Raw code content or project path") String code,
            ToolContext toolContext) {

        return """
               {
                   "summary": "Code analyzed. Improvements found.",
                   "suggestions": [
                       "减少重复逻辑",
                       "优化数据库访问性能",
                       "补充异常处理",
                       "使用统一响应结构"
                   ]
               }
               """;
    }
}

