package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@Data
public class CommunityUnreadSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8383379938463509870L;

    @Schema(description = "未读评论数")
    private Long unreadCommentCount;

    @Schema(description = "未读点赞数")
    private Long unreadLikeCount;

    @Schema(description = "未读分享数")
    private Long unreadShareCount;

    @Schema(description = "未读总数")
    private Long totalUnreadCount;
}
