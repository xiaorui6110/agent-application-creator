package com.xiaorui.agentapplicationcreator.agent.plan.service;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.ValidatedPlan;
import com.xiaorui.agentapplicationcreator.agent.plan.result.ExecutionResult;

/**
 * @description: 计划执行器
 * @author: xiaorui
 * @date: 2026-01-05 21:07
 **/
public interface PlanExecutor {

    /**
     * 执行计划
     * @param plan 计划
     * @return 执行结果
     */
    ExecutionResult execute(ValidatedPlan plan);

}
