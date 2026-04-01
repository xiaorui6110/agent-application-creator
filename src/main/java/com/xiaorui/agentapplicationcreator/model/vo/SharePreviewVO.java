package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@Data
public class SharePreviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8568375717973520316L;

    @Schema(description = "app id")
    private String appId;

    @Schema(description = "app name")
    private String appName;

    @Schema(description = "share url")
    private String shareUrl;

    @Schema(description = "QR code data URL")
    private String qrCodeDataUrl;
}
