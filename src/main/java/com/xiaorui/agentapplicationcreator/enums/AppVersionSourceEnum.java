package com.xiaorui.agentapplicationcreator.enums;

import lombok.Getter;

/**
 * @description: 应用版本来源
 * @author: xiaorui
 * @date: 2026-03-26 21:25
 **/
@Getter
public enum AppVersionSourceEnum {

    GENERATED("GENERATED"),
    DEPLOYED("DEPLOYED"),
    RESTORED("RESTORED");

    private final String value;

    AppVersionSourceEnum(String value) {
        this.value = value;
    }
}
