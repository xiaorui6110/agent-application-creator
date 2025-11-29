package com.xiaorui.agentapplicationcreator.execption;

import com.xiaorui.agentapplicationcreator.response.ResponseEnum;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import lombok.Getter;

/**
 * @description: 自定义业务异常
 * @author: xiaorui
 * @date: 2025-11-29 11:43
 **/
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private String code;

    /**
     * 对象
     */
    private Object object;

    /**
     * 服务器响应实体
     */
    private ServerResponseEntity<?> serverResponseEntity;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(ResponseEnum responseEnum) {
        super(responseEnum.getMsg());
        this.code = responseEnum.getValue();
    }

    public BusinessException(ResponseEnum responseEnum, String msg) {
        super(msg);
        this.code = responseEnum.getValue();
    }

    public BusinessException(ServerResponseEntity<?> serverResponseEntity) {
        this.serverResponseEntity = serverResponseEntity;
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = ResponseEnum.SHOW_FAIL.getValue();
    }

    public BusinessException(String msg, Object object) {
        super(msg);
        this.code = ResponseEnum.SHOW_FAIL.getValue();
        this.object = object;
    }
}
