package com.xiaorui.agentapplicationcreator.model.dto.appcomment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用评论添加请求
 * @author: xiaorui
 * @date: 2026-03-07 14:14
 **/
@Data
public class AppCommentAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1772553137729848915L;

    /**
     * 评论用户id
     */
    private String userId;

    /**
     * 被评论应用id
     */
    private String appId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 父评论id，null表示顶级评论
     */
    private String parentId;

}
