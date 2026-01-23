package com.xiaorui.agentapplicationcreator.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户发送邮箱验证码请求
 * @author: xiaorui
 * @date: 2025-11-30 13:50
 **/
@Data
public class UserSendEmailCodeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2779461217917182681L;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userEmail;

    /**
     * 验证码用途：register-注册，resetPassword-重置密码，changeEmail-修改邮箱（作为 enums 类，可做可不做，这里的 type 只是起到缓存 key标记作用）
     */
    @Schema(description = "验证码用途：register-注册，resetPassword-重置密码，changeEmail-修改邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

}