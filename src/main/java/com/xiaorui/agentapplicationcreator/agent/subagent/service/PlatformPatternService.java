package com.xiaorui.agentapplicationcreator.agent.subagent.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.PlatformPattern;

/**
 * 代码优化平台级经验表 服务层。
 *
 * @author xiaorui
 */
public interface PlatformPatternService extends IService<PlatformPattern> {

    /**
     * 保存代码优化平台级经验
     *
     * @param platformPattern 平台级经验
     */
    void savePlatformPattern(String platformPattern);

}
