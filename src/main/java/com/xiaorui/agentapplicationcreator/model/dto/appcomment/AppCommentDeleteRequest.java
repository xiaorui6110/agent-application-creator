package com.xiaorui.agentapplicationcreator.model.dto.appcomment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用评论删除请求
 * @author: xiaorui
 * @date: 2026-03-07 14:14
 **/
@Data
public class AppCommentDeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3725247372658283462L;

    /**
     * 评论id
     */
    private String commentId;

}
