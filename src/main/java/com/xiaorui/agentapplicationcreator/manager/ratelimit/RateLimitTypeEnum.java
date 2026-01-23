package com.xiaorui.agentapplicationcreator.manager.ratelimit;

import lombok.Getter;

/**
 * @description: 限流类型枚举
 * @author: xiaorui
 * @date: 2026-01-21 01:08
 **/
@Getter
public enum RateLimitTypeEnum {
    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP
}
