package com.xiaorui.agentapplicationcreator.response;

import lombok.Getter;

/**
 * @description: 响应枚举（区别于之前的 ErrorCode 错误码枚举）
 * @author: xiaorui
 * @date: 2025-11-29 16:01
 **/
public enum ResponseEnum {

    /**
     * ok
     */
    OK("00000", "ok"),

    /**
     * 用于直接显示提示用户的错误，内容由输入内容决定
     */
    SHOW_FAIL("A00001", ""),

    /**
     * 用于直接显示提示系统的成功，内容由输入内容决定
     */
    SHOW_SUCCESS("A00002", ""),

    /**
     * 未授权
     */
    UNAUTHORIZED("A00004", "Unauthorized"),

    /**
     * 服务器出了点小差
     */
    EXCEPTION("A00005", "服务器出了点小差"),

    /**
     * 方法参数没有校验，内容由输入内容决定
     */
    METHOD_ARGUMENT_NOT_VALID("A00014", "方法参数没有校验");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 信息
     */
    @Getter
    private final String msg;

    public String getValue() {
        return code;
    }

    ResponseEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResponseEnum{" + "code='" + code + '\'' + ", msg='" + msg + '\'' + "} " + super.toString();
    }

}
