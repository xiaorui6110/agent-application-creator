package com.xiaorui.agentapplicationcreator.agent.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

/**
 * @description: 测试用（获取当前北京时间，大语言模型是无法直接访问专有知识库或获取实时动态信息） 直接工具（tools）
 * @author: xiaorui
 * @date: 2025-12-12 14:14
 **/

public class ExampleTestTool implements BiFunction<String, ToolContext, String> {

    /**
     * 定义北京时间格式化器（UTC+8 时区）
     */
    private static final DateTimeFormatter BEIJING_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public String apply(
            @ToolParam(description = "用户的提问") String query,
            ToolContext toolContext) {

        // 获取上海时区（北京时间）的当前时间
        LocalDateTime beijingTime = LocalDateTime.now(BEIJING_ZONE);
        // 格式化返回
        return String.format("当前北京时间：%s", beijingTime.format(BEIJING_TIME_FORMATTER));
    }
}
