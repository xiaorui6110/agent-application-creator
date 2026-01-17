package com.xiaorui.agentapplicationcreator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 【管理员】应用更新信息请求
 * @author: xiaorui
 * @date: 2025-12-31 14:16
 **/
@Data
public class AppAdminUpdateInfoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2411589890117553962L;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String appCover;

    /**
     * 应用描述
     */
    private String appDescription;

    /**
     * 应用排序优先级
     */
    private Integer appPriority;

    /**
     * 代码生成类型（枚举）（待定，可能后续修改，目前未使用）
     */
    private String codeGenType;
}
