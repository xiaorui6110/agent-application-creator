package com.xiaorui.agentapplicationcreator.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户修改密码请求
 * @author: xiaorui
 * @date: 2025-11-30 13:51
 **/
@Data
public class UserChangePasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1096558290800406929L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

}

