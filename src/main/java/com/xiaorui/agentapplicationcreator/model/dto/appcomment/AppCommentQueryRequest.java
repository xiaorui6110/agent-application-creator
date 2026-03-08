package com.xiaorui.agentapplicationcreator.model.dto.appcomment;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用评论查询请求
 * @author: xiaorui
 * @date: 2026-03-07 14:15
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AppCommentQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6273280142149965543L;

    /**
     * 应用id
     */
    private String appId;

}
