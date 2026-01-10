package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @description: Agent 行为记忆（不是"聊天记忆"，而是"行为偏差"）
 * @author: xiaorui
 * @date: 2026-01-10 14:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentBehaviorMemory {

    /**
     * Agent 哪种行为经常出错
     */
    private String failurePattern;

    /**
     * 这个错误导致了什么后果
     */
    private String consequence;

    /**
     * 下次应该如何修正
     */
    private String correctionHint;

    /**
     * 出现次数
     */
    private int frequency;

    /**
     * 最后一次发生时间
     */
    private Instant lastSeen;

}

