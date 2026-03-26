package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AppVersionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3430578735621175588L;

    @Schema(description = "应用版本id")
    private String appVersionId;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "版本号")
    private Integer versionNumber;

    @Schema(description = "版本来源")
    private String versionSource;

    @Schema(description = "版本备注")
    private String versionNote;

    @Schema(description = "快照目录")
    private String snapshotPath;

    @Schema(description = "入口文件")
    private String entryFile;

    @Schema(description = "部署地址")
    private String deployUrl;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
