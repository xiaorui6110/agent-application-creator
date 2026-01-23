package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "accessToken" )
    private String accessToken;

    /**
     * 刷新token
     */
    @Schema(description = "refreshToken" )
    private String refreshToken;

    /**
     * 过期时间
     */
    @Schema(description = "expiresTime" )
    private Integer expiresTime;
}