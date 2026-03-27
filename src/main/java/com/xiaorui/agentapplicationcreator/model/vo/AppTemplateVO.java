package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaorui
 */
@Data
public class AppTemplateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7290569413596826137L;

    @Schema(description = "模板id")
    private String templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板描述")
    private String templateDescription;

    @Schema(description = "代码生成类型")
    private String codeGenType;

    @Schema(description = "入口文件")
    private String entryFile;

    @Schema(description = "来源应用id")
    private String sourceAppId;

    @Schema(description = "创建用户id")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
