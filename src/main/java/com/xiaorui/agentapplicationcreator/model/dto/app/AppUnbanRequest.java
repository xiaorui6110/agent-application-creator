package com.xiaorui.agentapplicationcreator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用封禁/解封请求
 * @author: xiaorui
 * @date: 2025-12-22 11:11
 **/
@Data
public class AppUnbanRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -8082436504358387914L;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 操作类型：true-解禁，false-封禁
     */
    private Boolean isUnban;

}
