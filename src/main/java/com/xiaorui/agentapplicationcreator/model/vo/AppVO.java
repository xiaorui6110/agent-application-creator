package com.xiaorui.agentapplicationcreator.model.vo;

import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaorui
 */
@Data
public class AppVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3583557824430216469L;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用封面")
    private String appCover;

    @Schema(description = "应用初始化 prompt")
    private String appInitPrompt;

    @Schema(description = "应用描述")
    private String appDescription;

    @Schema(description = "代码生成类型")
    private CodeGenTypeEnum codeGenType;

    @Schema(description = "应用排序优先级")
    private Integer appPriority;

    @Schema(description = "应用分类")
    private String appCategory;

    @Schema(description = "推荐分")
    private Double recommendScore;

    @Schema(description = "部署访问地址")
    private String deployUrl;

    @Schema(description = "部署时间")
    private LocalDateTime deployedTime;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "分享数")
    private Long shareCount;

    @Schema(description = "浏览量")
    private Long viewCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建用户信息")
    private UserVO userVO;
}
