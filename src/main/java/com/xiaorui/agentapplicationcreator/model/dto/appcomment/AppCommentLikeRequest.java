package com.xiaorui.agentapplicationcreator.model.dto.appcomment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用评论点赞请求
 * @author: xiaorui
 * @date: 2026-03-07 14:15
 **/
@Data
public class AppCommentLikeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5318813448247296052L;

    /**
     * 评论id
     */
    private String commentId;

    /**
     * 点赞用户id
     */
    private String userId;

    /**
     * 点赞类型
     */
    private LikeType likeType;

    public enum LikeType {
        /**
         * 点赞
         */
        LIKE,
        /**
         * 取消点赞
         */
        CANCEL_LIKE
    }

    /**
     *  点赞评论
     */
    private Long likeCount;

    /**
     *  点踩评论
     */
    private Long dislikeCount;

}
