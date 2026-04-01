package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaorui
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -76161459138373646L;

    @Schema(description = "user id")
    private String userId;

    @Schema(description = "nickname")
    private String nickName;

    @Schema(description = "user avatar")
    private String userAvatar;

    @Schema(description = "user email")
    private String userEmail;

    @Schema(description = "user sex: m or f")
    private String userSex;

    @Schema(description = "user birthday: yyyy-mm-dd")
    private String userBirthday;

    @Schema(description = "user profile")
    private String userProfile;

    @Schema(description = "user role")
    private String userRole;

    @Schema(description = "user status: 1 normal, 2 banned")
    private Integer userStatus;

    @Schema(description = "create time")
    private LocalDateTime createTime;
}
