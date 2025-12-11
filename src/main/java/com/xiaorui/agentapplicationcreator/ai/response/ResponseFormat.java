package com.xiaorui.agentapplicationcreator.ai.response;

import lombok.Data;

/**
 * @description: 使用 Java 类定义响应格式
 * @author: xiaorui
 * @date: 2025-12-10 13:40
 **/

@Data
public class ResponseFormat {
    /**
     * 一个双关语响应（始终必需）
     */
    private String agentResponse;

    /**
     * 如果可用的话，关于应用的其他信息（TODO String 内使用 JSON 格式，之后提取一下（ResponseFormat））
     */
    private String appConditions;

}