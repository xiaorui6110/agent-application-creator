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
 * 代码优化问题清单表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_code_optimization_issue")
public class CodeOptimizationIssue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 问题id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String issueId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 问题级别，INFO / WARN / ERROR
     */
    private String level;

    /**
     * 问题类型，ARCHITECTURE / STYLE / BUG / SMELL
     */
    private String type;

    /**
     * 问题路径
     */
    private String path;

    /**
     * 问题消息
     */
    private String message;

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
