package com.xiaorui.agentapplicationcreator.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: token信息bo
 * @author: xiaorui
 * @date: 2025-11-30 14:07
 **/
@Data
public class TokenInfoBO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5989212831070419460L;

    /**
     * 保存在token信息里面的用户信息
     */
    @Schema(description = "保存在token信息里面的用户信息")
    private UserInfoInTokenBO userInfoInToken;

    /**
     * 访问token
     */
    @Schema(description = "访问token")
    private String accessToken;

    /**
     * 刷新token
     */
    @Schema(description = "刷新token")
    private String refreshToken;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private Integer expiresTime;

}
