package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用评论用户vo
 * @author: xiaorui
 * @date: 2026-03-07 14:18
 **/
@Data
public class AppCommentUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1476432195561970751L;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

}
