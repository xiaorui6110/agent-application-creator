package com.xiaorui.agentapplicationcreator.agent.subagent.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizationRun;

/**
 * 代码优化审计主表 服务层。
 *
 * @author xiaorui
 */
public interface CodeOptimizationRunService extends IService<CodeOptimizationRun> {

    /**
     * 保存代码优化审计记录
     *
     * @param codeOptimizationInput  代码优化输入
     * @param codeOptimizationResult 代码优化结果
     */
    void saveCodeOptimizationRun(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult);

}
