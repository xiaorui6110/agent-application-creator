package com.xiaorui.agentapplicationcreator.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description: 代码生成类型枚举
 * @author: xiaorui
 * @date: 2025-12-22 11:38
 **/
@Getter
public enum CodeGenTypeEnum {

    /**
     * 代码生成类型枚举
     */
    SINGLE_FILE("原生 HTML 模式", "single_file"),
    MULTI_FILE("原生多文件模式", "multi_file"),
    VUE_PROJECT("Vue 工程模式", "vue_project");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
