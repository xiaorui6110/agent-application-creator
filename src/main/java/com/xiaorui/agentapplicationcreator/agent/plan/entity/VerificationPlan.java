package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 验证计划（结果验证，不是给 Agent 用的）
 * @author: xiaorui
 * @date: 2026-01-05 20:41
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPlan {

    /**
     * 验证类型枚举
     */
    public enum Type {
        /**
         * 内容相等校验
         */
        CONTENT_EQUALS
    }

    /**
     * 验证类型
     */
    @JsonProperty("type")
    private Type type;

    /**
     * 相对 code_output 的路径
     */
    @JsonProperty("path")
    private String path;

    /**
     * 验证值
     */
    @JsonProperty("value")
    private String value;
}
