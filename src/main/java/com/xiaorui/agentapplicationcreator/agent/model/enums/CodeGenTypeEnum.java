package com.xiaorui.agentapplicationcreator.agent.model.enums;

import java.util.Arrays;

/**
 * @description: 应用代码生成类型
 * @author: xiaorui
 * @date: 2026-03-26 20:20
 **/
public enum CodeGenTypeEnum {

    /**
     * 单文件
     * 多文件
     * vue项目
     */
    SINGLE_FILE("single_file"),
    MULTI_FILE("multi_file"),
    VUE_PROJECT("vue_project");

    private final String value;

    CodeGenTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(item -> item.getValue().equals(value));
    }
}
