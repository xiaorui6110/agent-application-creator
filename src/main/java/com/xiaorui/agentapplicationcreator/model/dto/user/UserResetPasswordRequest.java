package com.xiaorui.agentapplicationcreator.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户重置密码请求
 * @author: xiaorui
 * @date: 2025-11-30 14:00
 **/
@Data
public class UserResetPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7967713214226772029L;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码（邮箱验证码）
     */
    private String emailVerifyCode;
}
