package com.xiaorui.agentapplicationcreator.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description: 静态资源访问类型枚举
 * @author: xiaorui
 * @date: 2026-01-17 12:15
 **/
@Getter
public enum StaticVisitTypeEnum {
    /**
     * 静态资源访问类型枚举
     */
    PREVIEW("预览", "preview"),
    DEPLOY("部署", "deploy");;

    private final String text;
    private final String value;

    StaticVisitTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static StaticVisitTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (StaticVisitTypeEnum anEnum : StaticVisitTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


}
