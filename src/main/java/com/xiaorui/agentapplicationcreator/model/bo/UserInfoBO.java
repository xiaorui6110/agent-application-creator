package com.xiaorui.agentapplicationcreator.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户信息bo
 * @author: xiaorui
 * @date: 2025-11-30 14:06
 **/
@Data
public class UserInfoBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 263278901439346230L;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String userId;

    /**
     * 用户角色 user-普通用户 admin-管理员
     */
    @Schema(description = "用户角色")
    private String userRole;

    /**
     * 用户状态 1-正常 2-禁用
     */
    @Schema(description = "用户状态")
    private Integer userStatus;

}