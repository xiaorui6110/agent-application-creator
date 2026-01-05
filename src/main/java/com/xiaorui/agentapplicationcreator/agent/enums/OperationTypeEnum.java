package com.xiaorui.agentapplicationcreator.agent.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description: 文件操作枚举
 * @author: xiaorui
 * @date: 2026-01-05 20:30
 **/
@Getter
public enum OperationTypeEnum {
    /**
     * 文件操作
     */
    CREATE_FILE(1),
    OVERWRITE_FILE(2),
    DELETE_FILE(3),
    MOVE_FILE(4);

    private final Integer value;

    OperationTypeEnum(Integer value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static OperationTypeEnum getEnumByValue(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (OperationTypeEnum anEnum : OperationTypeEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
