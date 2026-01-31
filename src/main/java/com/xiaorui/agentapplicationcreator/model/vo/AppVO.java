package com.xiaorui.agentapplicationcreator.model.vo;

import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 应用vo
 * @author: xiaorui
 * @date: 2025-12-22 10:59
 **/
@Data
public class AppVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3583557824430216469L;

    /**
     * 应用id（雪花算法）
     */
    @Schema(description = "应用id（雪花算法）")
    private String appId;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用封面
     */
    @Schema(description = "应用封面")
    private String appCover;

    /**
     * 应用初始化的 prompt
     */
    @Schema(description = "应用初始化的 prompt")
    private String appInitPrompt;

    /**
     * 应用描述
     */
    @Schema(description = "应用描述")
    private String appDescription;

    /**
     * 代码生成类型（枚举）
     */
    @Schema(description = "代码生成类型（枚举）")
    private CodeGenTypeEnum codeGenType;

    /**
     * 应用排序优先级
     */
    @Schema(description = "应用排序优先级")
    private Integer appPriority;

    /**
     * 部署访问地址
     */
    @Schema(description = "部署访问地址")
    private String deployUrl;

    /**
     * 部署时间
     */
    @Schema(description = "部署时间")
    private LocalDateTime deployedTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建用户信息
     */
    @Schema(description = "创建用户信息")
    private UserVO userVO;

}
