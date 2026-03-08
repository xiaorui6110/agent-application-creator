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
 * 分享记录表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_share_record")
public class ShareRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分享记录id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String shareId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 被分享内容id
     */
    private String targetId;

    /**
     * 被分享内容所属用户id
     */
    private String targetUserId;

    /**
     * 是否分享 0-取消 1-分享
     */
    private Integer isShared;

    /**
     * 分享时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shareTime;

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

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
