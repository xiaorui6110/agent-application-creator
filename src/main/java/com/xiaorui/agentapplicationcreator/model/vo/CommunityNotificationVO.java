package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommunityNotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6990568827800254671L;

    @Schema(description = "消息类型 COMMENT/LIKE/SHARE")
    private String notificationType;

    @Schema(description = "消息记录id")
    private String notificationId;

    @Schema(description = "触发用户id")
    private String actorUserId;

    @Schema(description = "触发用户昵称")
    private String actorUserName;

    @Schema(description = "关联应用id")
    private String appId;

    @Schema(description = "关联应用名称")
    private String appName;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息时间")
    private LocalDateTime actionTime;
}
