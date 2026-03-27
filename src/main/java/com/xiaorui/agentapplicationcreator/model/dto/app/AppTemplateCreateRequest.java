package com.xiaorui.agentapplicationcreator.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@Data
public class AppTemplateCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3279381903654305295L;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板描述")
    private String templateDescription;
}
