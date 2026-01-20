package com.xiaorui.agentapplicationcreator.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description: Agent 失败类型枚举
 * @author: xiaorui
 * @date: 2026-01-20 19:52
 **/
@Getter
public enum AgentFailTypeEnum {

    /**
     * Agent 失败类型枚举
     */
    SYSTEM_RETRYABLE("系统可重试", "system_retryable"),
    BUSINESS_FATAL("业务失败", "business_fatal");

    private final String text;
    private final String value;

    AgentFailTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AgentFailTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AgentFailTypeEnum anEnum : AgentFailTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
