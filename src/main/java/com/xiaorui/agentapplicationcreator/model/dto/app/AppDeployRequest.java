package com.xiaorui.agentapplicationcreator.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用部署请求
 * @author: xiaorui
 * @date: 2025-12-22 11:12
 **/
@Data
public class AppDeployRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4633762994535344463L;

    /**
     * 应用id
     */
    @Schema(description = "应用id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

}
