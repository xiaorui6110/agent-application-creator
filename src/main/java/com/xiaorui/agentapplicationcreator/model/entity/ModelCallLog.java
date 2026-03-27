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
 * 模型调用记录
 * @author xiaorui
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_model_call_log")
public class ModelCallLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模型调用记录id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String modelCallLogId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 对话线程id
     */
    private String threadId;

    /**
     * 对话id
     */
    private String conversationId;
    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 模型提供者
     */
    private String provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型调用类型
     */
    private String callType;

    /**
     * 调用状态
     */
    private String callStatus;

    /**
     * 提示词token数
     */
    private Integer promptTokens;

    /**
     * 完成词token数
     */
    private Integer completionTokens;

    /**
     * 总token数
     */
    private Integer totalTokens;

    /**
     * 延迟ms
     */
    private Long latencyMs;

    /**
     * 错误信息
     */
    private String errorMessage;

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
