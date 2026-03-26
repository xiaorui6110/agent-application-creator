package com.xiaorui.agentapplicationcreator.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppAdminUpdateInfoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2411589890117553962L;

    @Schema(description = "应用id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用封面")
    private String appCover;

    @Schema(description = "应用描述")
    private String appDescription;

    @Schema(description = "应用排序优先级")
    private Integer appPriority;

    @Schema(description = "应用分类")
    private String appCategory;

    @Schema(description = "推荐分")
    private Double recommendScore;

    @Schema(description = "代码生成类型")
    private String codeGenType;
}
