package com.xiaorui.agentapplicationcreator.agent.subagent.model.dto;

import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.FilePatch;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.Issue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description: 代码优化结果
 * @author: xiaorui
 * @date: 2026-01-13 16:38
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeOptimizationResult {

    /**
     * 优化结果的概要
     */
    private String summary;

    /**
     * 优化结果的详细信息
     */
    private List<Issue> issues;

    /**
     * 优化结果的建议（直接可以应用的文件级修改）
     */
    private List<FilePatch> suggestedDiff;

    /**
     * 新模式（平台经验）
     */
    private List<String> newPatterns;

    /**
     * 置信度
     */
    private double confidence;
}
