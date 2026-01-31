package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户vo
 * @author: xiaorui
 * @date: 2025-11-30 14:02
 **/
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -76161459138373646L;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户性别 m-男 f-女
     */
    @Schema(description = "用户性别 m-男 f-女")
    private String userSex;

    /**
     * 用户生日 yyyy-mm-dd
     */
    @Schema(description = "用户生日 yyyy-mm-dd")
    private String userBirthday;

    /**
     * 用户备注
     */
    @Schema(description = "用户备注")
    private String userProfile;

    /**
     *  用户角色 user-普通用户 admin-管理员
     */
    @Schema(description = "用户角色")
    private String userRole;

}
