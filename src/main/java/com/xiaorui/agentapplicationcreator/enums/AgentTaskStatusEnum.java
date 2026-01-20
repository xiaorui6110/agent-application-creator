package com.xiaorui.agentapplicationcreator.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description: 任务状态枚举
 * @author: xiaorui
 * @date: 2026-01-19 19:44
 **/
@Getter
public enum AgentTaskStatusEnum {

    /**
     * 任务状态枚举
     */
    INIT("任务初始化中", "init"),
    QUEUED("任务排队中", "queued"),
    RUNNING("任务运行中", "running"),
    SUCCEEDED("任务已完成", "succeeded"),
    FAILED("任务运行失败", "failed"),
    RETRY_WAITING("重试等待中", "retry_waiting");

    private final String text;
    private final String value;

    AgentTaskStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AgentTaskStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AgentTaskStatusEnum anEnum : AgentTaskStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
