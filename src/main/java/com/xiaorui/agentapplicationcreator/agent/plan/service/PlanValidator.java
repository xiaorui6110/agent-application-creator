package com.xiaorui.agentapplicationcreator.agent.plan.service;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.CodeModificationPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;

/**
 * @description: 计划验证器
 * @author: xiaorui
 * @date: 2026-01-05 20:54
 **/
public interface PlanValidator {

    /**
     * 验证计划
     * @param plan 计划
     * @return 验证结果
     */
    ValidatedPlan validate(CodeModificationPlan plan);

}

