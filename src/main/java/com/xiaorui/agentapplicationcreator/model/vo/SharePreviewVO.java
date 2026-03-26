package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SharePreviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8568375717973520316L;

    @Schema(description = "应用 ID")
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "分享链接")
    private String shareUrl;

    @Schema(description = "二维码 Data URL")
    private String qrCodeDataUrl;
}
