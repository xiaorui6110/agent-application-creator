package com.xiaorui.agentapplicationcreator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用更新信息请求
 * @author: xiaorui
 * @date: 2025-12-22 11:10
 **/
@Data
public class AppUpdateInfoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4684092321724456996L;

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
     * 代码生成类型（枚举）（TODO 待定，可能可以后续修改）
     */
    private String codeGenType;

}
