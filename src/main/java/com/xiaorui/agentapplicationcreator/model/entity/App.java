package com.xiaorui.agentapplicationcreator.model.entity;

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
 * 应用表 实体类。
 *
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
     * 应用id（雪花算法）
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
     * 应用初始化的 prompt
     */
    private String appInitPrompt;

    /**
     * 应用描述
     */
    private String appDescription;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    /**
     * 部署唯一标识
     */
    private String deployKey;

    /**
     * 部署访问地址
     */
    private String deployUrl;

    /**
     * 部署时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime deployedTime;

    /**
     * 应用排序优先级
     */
    private Integer appPriority;

    /**
     * 创建用户id
     */
    private String userId;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
