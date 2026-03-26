package com.xiaorui.agentapplicationcreator.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppTemplateUseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2712913826784560870L;

    @Schema(description = "模板id")
    private String templateId;

    @Schema(description = "新应用名称")
    private String appName;

    @Schema(description = "新应用描述")
    private String appDescription;
}
