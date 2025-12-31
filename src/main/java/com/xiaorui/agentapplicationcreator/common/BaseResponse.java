package com.xiaorui.agentapplicationcreator.common;

import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 响应工具类（已弃用）
 * @author: xiaorui
 * @date: 2025-11-29 11:41
 **/
@Data
public class BaseResponse <T> implements Serializable {

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应信息
     */
    private String msg;

    public BaseResponse(String code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public BaseResponse(String code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMsg());
    }

}
