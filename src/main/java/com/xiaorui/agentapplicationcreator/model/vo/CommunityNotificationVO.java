package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaorui
 */
@Data
public class CommunityNotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6990568827800254671L;

    @Schema(description = "notification type: COMMENT, LIKE, SHARE")
    private String notificationType;

    @Schema(description = "notification record id")
    private String notificationId;

    @Schema(description = "actor user id")
    private String actorUserId;

    @Schema(description = "actor user name")
    private String actorUserName;

    @Schema(description = "related app id")
    private String appId;

    @Schema(description = "related app name")
    private String appName;

    @Schema(description = "notification content")
    private String content;

    @Schema(description = "notification time")
    private LocalDateTime actionTime;
}
