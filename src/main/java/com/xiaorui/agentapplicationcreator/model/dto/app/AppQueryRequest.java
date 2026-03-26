package com.xiaorui.agentapplicationcreator.model.dto.app;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8008136519720523420L;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "代码生成类型")
    private String codeGenType;

    @Schema(description = "应用排序优先级")
    private Integer appPriority;

    @Schema(description = "应用分类")
    private String appCategory;

    @Schema(description = "排行类型 hot/recommend/latest")
    private String rankType;
}
