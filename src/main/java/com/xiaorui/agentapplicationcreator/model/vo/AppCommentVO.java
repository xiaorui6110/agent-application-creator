package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 应用评论vo
 * @author: xiaorui
 * @date: 2026-03-07 14:16
 **/
@Data
public class AppCommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8911539560498743499L;

    /**
     * 评论id
     */
    @Schema(description = "评论id")
    private String commentId;

    /**
     * 评论用户id
     */
    @Schema(description = "评论用户id")
    private String userId;

    /**
     * 被评论应用id
     */
    @Schema(description = "被评论应用id")
    private String appId;

    /**
     * 被评论应用所属用户id
     */
    @Schema(description = "被评论应用所属用户id")
    private String appUserId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String commentContent;

    /**
     * 父评论id，null表示顶级评论
     */
    @Schema(description = "父评论id，null表示顶级评论")
    private String parentId;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Long likeCount;

    /**
     * 点踩数
     */
    @Schema(description = "点踩数")
    private Long dislikeCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 子评论列表
     */
    @Schema(description = "子评论列表")
    private List<AppCommentVO> childCommentList;

    /**
     * 评论用户信息
     */
    @Schema(description = "评论用户信息")
    private AppCommentUserVO appCommentUserVO;

    /**
     * 被评论应用信息
     */
    @Schema(description = "被评论应用信息")
    private AppVO appVO;

}
