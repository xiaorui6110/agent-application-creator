package com.xiaorui.agentapplicationcreator.agent.plan.service.impl;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.AgentBehaviorMemory;
import com.xiaorui.agentapplicationcreator.agent.plan.service.AgentMemoryStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 内存存储 Agent 行为记忆
 * @author: xiaorui
 * @date: 2026-01-10 17:07
 **/
@Component
public class InMemoryAgentMemoryStore implements AgentMemoryStore {

    private final Map<String, AgentBehaviorMemory> store = new ConcurrentHashMap<>();

    /**
     * 保存或更新 Agent 行为记忆 TODO 先使用内存实现，后续使用 Redis 或数据库实现
     * @param memory Agent 行为记忆
     */
    @Override
    public synchronized void saveOrUpdate(AgentBehaviorMemory memory) {
        store.merge(memory.getFailurePattern(), memory, (oldMem, newMem) -> {
            oldMem.setFrequency(oldMem.getFrequency() + 1);
            oldMem.setLastSeen(Instant.now());
            oldMem.setConsequence(newMem.getConsequence());
            oldMem.setCorrectionHint(newMem.getCorrectionHint());
            return oldMem;
        });
    }

    /**
     * 获取所有 Agent 行为记忆
     * @return 所有 Agent 行为记忆
     */
    @Override
    public List<AgentBehaviorMemory> getAll() {
        return new ArrayList<>(store.values());
    }
}

