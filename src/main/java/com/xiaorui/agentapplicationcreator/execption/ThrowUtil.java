package com.xiaorui.agentapplicationcreator.execption;

/**
 * @description: 异常处理工具类
 * @author: xiaorui
 * @date: 2025-11-29 11:43
 **/

public class ThrowUtil {
    /**
     * 条件成立则抛异常
     *
     * @param condition        条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param msg 错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String msg) {
        throwIf(condition, new BusinessException(errorCode, msg));
    }


}
