package com.xiaorui.agentapplicationcreator.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppVersionRestoreRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7999410682183587499L;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "应用版本id")
    private String appVersionId;
}
