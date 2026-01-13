package com.xiaorui.agentapplicationcreator.agent.subagent.model.dto;

import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeChange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description: 代码优化输入
 * @author: xiaorui
 * @date: 2026-01-13 16:35
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeOptimizationInput {

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用目标
     */
    private String appGoal;

    /**
     * 技术栈
     */
    private List<String> techStack;

    /**
     * 文件树 e.g. ["src/App.vue", "src/components/Timer.vue"]
     */
    private List<String> fileTree;

    /**
     * 文件内容 path -> content
     */
    private Map<String, String> files;

    /**
     * 最近一次用户触发的改动
     */
    private List<CodeChange> recentChanges;

    /**
     * 平台的长期记忆 TODO 落库存储
     */
    private List<String> platformMemory;

}

