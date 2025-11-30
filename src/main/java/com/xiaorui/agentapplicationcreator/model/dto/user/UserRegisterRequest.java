package com.xiaorui.agentapplicationcreator.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户注册请求
 * @author: xiaorui
 * @date: 2025-11-30 13:50
 **/
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4133028864960991598L;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码（邮箱验证码）
     */
    private String emailVerifyCode;

}
