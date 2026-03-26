package com.xiaorui.agentapplicationcreator.agent.model.enums;

import java.util.Arrays;

/**
 * @description: Agent 顶层输出类型
 * @author: xiaorui
 * @date: 2026-03-26 20:20
 **/
public enum AgentResponseTypeEnum {

    /**
     * 需求澄清
     */
    CLARIFICATION,

    /**
     * 生成模式确认
     */
    MODE_SELECTION,

    /**
     * 方案设计
     */
    SOLUTION_DESIGN,

    /**
     * 代码生成
     */
    CODE_GENERATION,

    /**
     * 代码修改规划
     */
    CODE_MODIFICATION;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(item -> item.name().equals(value));
    }
}
