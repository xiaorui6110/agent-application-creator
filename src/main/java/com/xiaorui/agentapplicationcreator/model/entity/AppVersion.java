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
@Table("xr_app_version")
public class AppVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用版本id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String appVersionId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用版本号
     */
    private Integer versionNumber;

    /**
     * 应用版本来源
     */
    private String versionSource;

    /**
     * 应用版本备注
     */
    private String versionNote;

    /**
     * 应用版本快照路径
     */
    private String snapshotPath;

    /**
     * 应用版本入口文件
     */
    private String entryFile;

    /**
     * 应用版本部署url
     */
    private String deployUrl;

    /**
     * 创建人
     */
    private String createdBy;

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
