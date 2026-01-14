package com.xiaorui.agentapplicationcreator.agent.subagent.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizationPatch;

/**
 * 代码优化修改建议表 服务层。
 *
 * @author xiaorui
 */
public interface CodeOptimizationPatchService extends IService<CodeOptimizationPatch> {

    /**
     * 保存代码优化修改建议
     *
     * @param codeOptimizationInput  代码优化输入
     * @param codeOptimizationResult 代码优化结果
     */
    void saveCodeOptimizationPatch(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult);

}
