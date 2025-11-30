package com.xiaorui.agentapplicationcreator.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户登录请求
 * @author: xiaorui
 * @date: 2025-11-30 13:50
 **/
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2060985167877199901L;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 验证码（图形数字验证码-用户输入的）
     */
    private String verifyCode;

    /**
     * 验证码（数字验证码-服务器存储的）
     */
    private String serverVerifyCode;

}