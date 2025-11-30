package com.xiaorui.agentapplicationcreator.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户修改邮箱请求
 * @author: xiaorui
 * @date: 2025-11-30 13:50
 **/
@Data
public class UserChangeEmailRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2573793437719393389L;

    /**
     * 新邮箱
     */
    private String newEmail;

    /**
     * 验证码（邮箱验证码）
     */
    private String emailVerifyCode;

}