package com.xiaorui.agentapplicationcreator.enums;

import lombok.Getter;

/**
 * @description: 系统类型枚举（已弃用）
 * @author: xiaorui
 * @date: 2025-11-29 16:10
 **/
@Getter
public enum SysTypeEnum {
    /**
     * 普通用户系统
     */
    ORDINARY(0),

    /**
     * 后台
     */
    ADMIN(1);

    private final Integer value;

    SysTypeEnum(Integer value) {
        this.value = value;
    }
}
