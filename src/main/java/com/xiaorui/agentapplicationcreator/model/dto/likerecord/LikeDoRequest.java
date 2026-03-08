package com.xiaorui.agentapplicationcreator.model.dto.likerecord;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 点赞请求
 * @author: xiaorui
 * @date: 2026-03-07 17:53
 **/
@Data
public class LikeDoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -5594468548704571958L;

    /**
     * 被点赞内容id
     */
    private String targetId;

    /**
     * 是否点赞 0-取消 1-点赞
     */
    private Integer isLiked;

}
