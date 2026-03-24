package com.xiaorui.agentapplicationcreator.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum AgentTaskStatusEnum {

    /**
     * 任务状态
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

    public String getApiValue() {
        if (this == INIT || this == QUEUED) {
            return "WAITING";
        }
        return this.name();
    }

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

    public static String toApiValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return "WAITING";
        }
        AgentTaskStatusEnum statusEnum = getEnumByValue(value);
        if (statusEnum != null) {
            return statusEnum.getApiValue();
        }
        String upperValue = value.trim().toUpperCase();
        if ("INIT".equals(upperValue) || "QUEUED".equals(upperValue)) {
            return "WAITING";
        }
        return upperValue;
    }
}
