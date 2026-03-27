package com.xiaorui.agentapplicationcreator.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@Data
public class UserRoleUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 id")
    private String userId;

    @Schema(description = "用户角色：admin / user")
    private String userRole;
}
