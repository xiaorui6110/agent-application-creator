package com.xiaorui.agentapplicationcreator.agent.subagent.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizationIssue;

/**
 * 代码优化问题清单表 服务层。
 *
 * @author xiaorui
 */
public interface CodeOptimizationIssueService extends IService<CodeOptimizationIssue> {

    /**
     * 保存代码优化问题清单
     *
     * @param codeOptimizationInput  代码优化输入
     * @param codeOptimizationResult 代码优化结果
     */
    void saveCodeOptimizationIssue(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult);

}
