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
 * 代码优化平台级经验表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_platform_pattern")
public class PlatformPattern implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 经验id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String patternId;

    /**
     * 经验文本
     */
    private String patternText;

    /**
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime lastSeenAt;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
