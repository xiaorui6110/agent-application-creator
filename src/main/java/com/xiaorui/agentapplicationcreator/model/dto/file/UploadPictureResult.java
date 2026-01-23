package com.xiaorui.agentapplicationcreator.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @description: 上传图片结果
 * @author: xiaorui
 * @date: 2025-12-02 11:28
 **/
@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    @Schema(description = "图片地址")
    private String picUrl;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称")
    private String picName;

    /**
     * 文件体积
     */
    @Schema(description = "文件体积")
    private Long picSize;

    /**
     * 图片格式
     */
    @Schema(description = "图片格式")
    private String picFormat;

}