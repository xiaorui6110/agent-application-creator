package com.xiaorui.agentapplicationcreator.model.dto.app;

import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * 应用排序优先级
     */
    @Schema(description = "应用排序优先级")
    private Integer appPriority;

    /**
     * 代码生成类型（枚举）
     */
    @Schema(description = "代码生成类型（枚举）")
    private CodeGenTypeEnum codeGenType;
}
