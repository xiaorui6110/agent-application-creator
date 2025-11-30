package com.xiaorui.agentapplicationcreator.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: token信息vo
 * @author: xiaorui
 * @date: 2025-11-30 14:04
 **/
@Data
public class TokenInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2719825461024555708L;

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;

    /**
     * 过期时间
     */
    private Integer expiresTime;
}