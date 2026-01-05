package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description: 验证计划（结果验证，不是给 Agent 用的）
 * @author: xiaorui
 * @date: 2026-01-05 20:41
 **/
@Data
@AllArgsConstructor
public class VerificationPlan {

    public enum Type {
        /**
         * 校验
         */
        CONTENT_EQUALS
    }

    /**
     * 验证类型
     */
    private Type type;

    /**
     * 相对 code_output 的路径
     */
    private String path;

    /**
     * 验证值
     */
    private String value;
}
