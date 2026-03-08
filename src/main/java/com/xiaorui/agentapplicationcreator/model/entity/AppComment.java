package com.xiaorui.agentapplicationcreator.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用评论表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_app_comment")
public class AppComment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String commentId;

    /**
     * 评论用户id
     */
    private String userId;

    /**
     * 被评论应用id
     */
    private String appId;

    /**
     * 被评论应用所属用户id
     */
    private String appUserId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 父评论id，null表示顶级评论
     */
    private String parentId;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 点踩数
     */
    private Long dislikeCount;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

    /**
     * 是否已读 0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
