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
@Table("xr_app_template")
public class AppTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String templateDescription;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 入口文件
     */
    private String entryFile;

    /**
     * 源应用id
     */
    private String sourceAppId;

    /**
     * 存储路径
     */
    private String storagePath;

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
