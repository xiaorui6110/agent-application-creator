package com.xiaorui.agentapplicationcreator.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户解封/封禁请求
 * @author: xiaorui
 * @date: 2025-11-30 13:59
 **/
@Data
public class UserUnbanRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1639353430126158956L;

    /**
     * 用户id
     */
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    /**
     * 操作类型：true-解禁，false-封禁
     */
    @Schema(description = "操作类型：true-解禁，false-封禁", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isUnban;
}