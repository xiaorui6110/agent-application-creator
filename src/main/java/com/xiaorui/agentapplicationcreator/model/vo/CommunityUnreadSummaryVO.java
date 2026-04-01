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

    @Schema(description = "unread comment count")
    private Long unreadCommentCount;

    @Schema(description = "unread like count")
    private Long unreadLikeCount;

    @Schema(description = "unread share count")
    private Long unreadShareCount;

    @Schema(description = "total unread count")
    private Long totalUnreadCount;
}
