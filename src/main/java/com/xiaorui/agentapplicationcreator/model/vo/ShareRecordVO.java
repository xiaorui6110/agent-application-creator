package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 分享记录vo
 * @author: xiaorui
 * @date: 2026-03-07 19:25
 **/
@Data
public class ShareRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4103371414900535007L;

    /**
     * 分享记录id
     */
    @Schema(description = "分享记录id")
    private String shareId;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String userId;

    /**
     * 被分享内容id
     */
    @Schema(description = "被分享内容id")
    private String targetId;

    /**
     * 分享时间
     */
    @Schema(description = "分享时间")
    private LocalDateTime shareTime;

    /**
     * 分享用户信息
     */
    @Schema(description = "分享用户信息")
    private UserVO userVO;

    /**
     * 被分享内容信息
     */
    @Schema(description = "被分享内容信息")
    private AppVO appVO;

}
