package com.xiaorui.agentapplicationcreator.model.dto.likerecord;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 点赞查询请求
 * @author: xiaorui
 * @date: 2026-03-07 17:53
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class LikeQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1040483361170912667L;

    /**
     * 被点赞内容id
     */
    private String targetId;

}
