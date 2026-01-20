package com.xiaorui.agentapplicationcreator.manager.ratelimit;

/**
 * @description: 限流类型枚举
 * @author: xiaorui
 * @date: 2026-01-21 01:08
 **/

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
