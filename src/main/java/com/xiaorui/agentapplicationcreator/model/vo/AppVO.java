package com.xiaorui.agentapplicationcreator.model.vo;

import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaorui
 */
@Data
public class AppVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3583557824430216469L;

    @Schema(description = "app id")
    private String appId;

    @Schema(description = "app name")
    private String appName;

    @Schema(description = "app cover")
    private String appCover;

    @Schema(description = "app initial prompt")
    private String appInitPrompt;

    @Schema(description = "app description")
    private String appDescription;

    @Schema(description = "code generation type")
    private CodeGenTypeEnum codeGenType;

    @Schema(description = "app priority")
    private Integer appPriority;

    @Schema(description = "app category")
    private String appCategory;

    @Schema(description = "recommendation score")
    private Double recommendScore;

    @Schema(description = "deploy url")
    private String deployUrl;

    @Schema(description = "deploy time")
    private LocalDateTime deployedTime;

    @Schema(description = "comment count")
    private Long commentCount;

    @Schema(description = "like count")
    private Long likeCount;

    @Schema(description = "share count")
    private Long shareCount;

    @Schema(description = "view count")
    private Long viewCount;

    @Schema(description = "create time")
    private LocalDateTime createTime;

    @Schema(description = "update time")
    private LocalDateTime updateTime;

    @Schema(description = "creator user info")
    private UserVO userVO;
}
