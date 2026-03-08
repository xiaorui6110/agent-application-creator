package com.xiaorui.agentapplicationcreator.model.dto.sharerecord;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 分享记录查询请求
 * @author: xiaorui
 * @date: 2026-03-07 19:28
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ShareQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1840286624339156804L;

    /**
     * 被分享内容id
     */
    private String targetId;
}
