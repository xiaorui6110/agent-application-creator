package com.xiaorui.agentapplicationcreator.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description: 用户状态枚举
 * @author: xiaorui
 * @date: 2025-11-29 16:10
 **/
@Getter
public enum UserStatusEnum {
    /**
     * 正常
     */
    NORMAL(1),

    /**
     * 禁用
     */
    BANNED(2);

    private final Integer value;

    UserStatusEnum(Integer value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserStatusEnum getEnumByValue(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserStatusEnum anEnum : UserStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }

}
