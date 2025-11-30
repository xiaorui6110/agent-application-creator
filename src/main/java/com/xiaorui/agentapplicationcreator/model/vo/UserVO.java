package com.xiaorui.agentapplicationcreator.model.vo;

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
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别 m-男 f-女
     */
    private String userSex;

    /**
     * 用户生日 yyyy-mm-dd
     */
    private String userBirthday;

    /**
     * 用户备注
     */
    private String userProfile;

}
