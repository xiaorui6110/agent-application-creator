package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description: 预期条件
 * @author: xiaorui
 * @date: 2026-01-05 20:38
 **/
@Data
@AllArgsConstructor
public class ExpectedCondition {

    public enum Type {
        /**
         * 校验
         */
        CONTENT_EQUALS,
        FILE_EXISTS,
        FILE_NOT_EXISTS
    }

    /**
     * 条件类型
     */
    private Type type;

    /**
     * 条件值
     */
    private String value;
}
