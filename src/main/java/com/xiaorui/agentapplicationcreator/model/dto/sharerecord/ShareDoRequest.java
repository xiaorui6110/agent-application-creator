package com.xiaorui.agentapplicationcreator.model.dto.sharerecord;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 分享请求
 * @author: xiaorui
 * @date: 2026-03-07 19:28
 **/
@Data
public class ShareDoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1140245884565858033L;

    /**
     * 被分享内容id
     */
    @JsonAlias("shareId")
    private String targetId;

    /**
     * 是否分享 0-取消 1-分享
     */
    private Integer isShared;

}
