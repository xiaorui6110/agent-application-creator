package com.xiaorui.agentapplicationcreator.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户更新信息请求
 * @author: xiaorui
 * @date: 2025-11-30 13:50
 **/
@Data
public class UserUpdateInfoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 7841698056715277267L;

    /**
     * 用户id
     */
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
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
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

}