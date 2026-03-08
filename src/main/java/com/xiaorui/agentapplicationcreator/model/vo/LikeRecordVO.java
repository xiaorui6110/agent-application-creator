package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 点赞记录vo
 * @author: xiaorui
 * @date: 2026-03-07 16:45
 **/
@Data
public class LikeRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3227904436127525534L;

    /**
     * 点赞记录id
     */
    @Schema(description = "点赞记录id")
    private String likeId;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String userId;

    /**
     * 被点赞内容id
     */
    @Schema(description = "被点赞内容id")
    private String targetId;

    /**
     * 被点赞内容所属用户id
     */
    @Schema(description = "被点赞内容所属用户id")
    private String targetUserId;

    /**
     * 是否点赞 0-取消 1-点赞
     */
    @Schema(description = "是否点赞 0-取消 1-点赞")
    private Integer isLiked;

    /**
     * 最近一次点赞时间
     */
    @Schema(description = "最近一次点赞时间")
    private LocalDateTime lastLikeTime;

    /**
     * 点赞用户信息
     */
    @Schema(description = "点赞用户信息")
    private UserVO userVO;

    /**
     * 被点赞内容信息
     */
    @Schema(description = "被点赞内容信息")
    private AppVO appVO;

}
