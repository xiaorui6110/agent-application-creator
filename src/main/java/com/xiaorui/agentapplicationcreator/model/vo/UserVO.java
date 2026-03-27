package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 用户 vo
 * @author: xiaorui
 * @date: 2025-11-30 14:02
 **/
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -76161459138373646L;

    @Schema(description = "用户 id")
    private String userId;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户邮箱")
    private String userEmail;

    @Schema(description = "用户性别 m-男 f-女")
    private String userSex;

    @Schema(description = "用户生日 yyyy-mm-dd")
    private String userBirthday;

    @Schema(description = "用户简介")
    private String userProfile;

    @Schema(description = "用户角色")
    private String userRole;

    @Schema(description = "用户状态 1-正常 2-封禁")
    private Integer userStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
