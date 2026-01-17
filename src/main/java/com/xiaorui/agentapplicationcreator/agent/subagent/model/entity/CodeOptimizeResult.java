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
 * 代码优化结果表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_code_optimize_result")
public class CodeOptimizeResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 代码优化历史id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String codeOptimizeHistoryId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 代码优化总结
     */
    private String codeOptimizeSummary;

    /**
     * 代码优化问题
     */
    private String codeOptimizeIssues;

    /**
     * 代码优化建议
     */
    private String codeOptimizeSuggestions;

    /**
     * 平台经验
     */
    private String platformExperience;

    /**
     * Agent 置信度
     */
    private Double agentConfidence;

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
