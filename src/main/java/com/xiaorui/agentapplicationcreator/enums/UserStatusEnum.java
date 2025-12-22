package com.xiaorui.agentapplicationcreator.enums;

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

}
