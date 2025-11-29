package com.xiaorui.agentapplicationcreator.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 服务器响应信息
 * @author: xiaorui
 * @date: 2025-11-29 16:02
 **/
@Data
public class ServerResponse<T> implements Serializable {

    /**
     * 响应码
     */
    private String code;

    /**
     * 信息
     */
    private String msg;

    /**
     * 对象
     */
    private T obj;

}
