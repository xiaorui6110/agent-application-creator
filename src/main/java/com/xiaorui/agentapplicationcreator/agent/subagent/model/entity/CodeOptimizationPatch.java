package com.xiaorui.agentapplicationcreator.agent.subagent.model.entity;

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
 * 代码优化修改建议表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_code_optimization_patch")
public class CodeOptimizationPatch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 修改建议id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String patchId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 操作类型，add / modify / delete
     */
    private String action;

    /**
     * 文件内容
     */
    private String content;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
