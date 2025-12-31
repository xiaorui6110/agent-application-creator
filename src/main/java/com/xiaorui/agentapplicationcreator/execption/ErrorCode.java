package com.xiaorui.agentapplicationcreator.execption;

import lombok.Getter;

/**
 * @description: 错误码
 * @author: xiaorui
 * @date: 2025-11-29 11:43
 **/
@Getter
public enum ErrorCode {
    /**
     * 状态码（code 改为 String 类型，方便扩展）
     */
    SUCCESS("0", "ok"),
    PARAMS_ERROR("40000", "请求参数错误"),
    NOT_LOGIN_ERROR("40100", "未登录"),
    NOT_AUTH_ERROR("40101", "无权限"),
    NOT_FOUND_ERROR("40400", "请求资源不存在"),
    FORBIDDEN_ERROR("40300", "禁止访问"),
    TOO_MANY_REQUEST("42900", "请求过于频繁"),
    SYSTEM_ERROR("50000", "系统内部异常"),
    OPERATION_ERROR("50001", "操作失败");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 信息
     */
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
