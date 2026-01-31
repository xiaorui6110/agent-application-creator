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
 * 用户-智能体会话绑定表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_user_thread_bind")
public class UserThreadBind implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 绑定ID（雪花算法）
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String bindId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * Agent 对话线程ID
     */
    private String threadId;

    /**
     * Agent 名称
     */
    private String agentName;

    /**
     * 绑定状态 0-未绑定 1-已绑定
     */
    private Integer bindStatus;

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
     * 是否删除 0-未删除 1-已删除（逻辑删除）
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
