package com.xiaorui.agentapplicationcreator.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_app")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String appCover;

    /**
     * 应用初始化提示
     */
    private String appInitPrompt;

    /**
     * 应用描述
     */
    private String appDescription;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 部署key
     */
    private String deployKey;

    /**
     * 部署url
     */
    private String deployUrl;

    /**
     * 部署时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deployedTime;

    /**
     * 应用优先级
     */
    private Integer appPriority;

    /**
     * 应用分类
     */
    private String appCategory;

    /**
     * 推荐分数
     */
    private Double recommendScore;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 分享数
     */
    private Long shareCount;

    /**
     * 访问量
     */
    private Long viewCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
