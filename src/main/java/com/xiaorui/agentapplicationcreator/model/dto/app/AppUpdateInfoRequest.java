package com.xiaorui.agentapplicationcreator.model.dto.app;

import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * 应用id
     */
    @Schema(description = "应用id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用封面
     */
    @Schema(description = "应用封面")
    private String appCover;

    /**
     * 应用描述
     */
    @Schema(description = "应用描述")
    private String appDescription;

    /**
     * 代码生成类型（枚举）
     */
    @Schema(description = "代码生成类型（枚举）")
    private CodeGenTypeEnum codeGenType;

}
