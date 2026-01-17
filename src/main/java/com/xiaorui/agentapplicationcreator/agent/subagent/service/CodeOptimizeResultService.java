package com.xiaorui.agentapplicationcreator.agent.subagent.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizeResult;

/**
 * 代码优化结果表 服务层。
 *
 * @author xiaorui
 */
public interface CodeOptimizeResultService extends IService<CodeOptimizeResult> {

    /**
     * 获取代码优化结果
     *
     * @param appId 应用id
     * @return 代码优化结果
     */
    CodeOptimizeResult getByAppId(String appId);

}
