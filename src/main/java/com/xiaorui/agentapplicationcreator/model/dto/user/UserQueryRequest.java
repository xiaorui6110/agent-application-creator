package com.xiaorui.agentapplicationcreator.model.dto.user;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4804821250622563683L;

    @Schema(description = "user id")
    private String userId;

    @Schema(description = "nickname")
    private String nickName;
}
