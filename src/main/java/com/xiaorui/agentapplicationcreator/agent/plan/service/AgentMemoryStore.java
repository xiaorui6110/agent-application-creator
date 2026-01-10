package com.xiaorui.agentapplicationcreator.agent.plan.service;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.AgentBehaviorMemory;

import java.util.List;

/**
 * @description: Agent 行为记忆存储服务
 * @author: xiaorui
 * @date: 2026-01-10 16:55
 **/
public interface AgentMemoryStore {

    /**
     * 保存或更新 Agent 行为记忆
     * @param memory Agent 行为记忆
     */
    void saveOrUpdate(AgentBehaviorMemory memory);

    /**
     * 获取所有 Agent 行为记忆
     * @return 所有 Agent 行为记忆
     */
    List<AgentBehaviorMemory> getAll();
}
