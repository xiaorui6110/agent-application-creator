package com.xiaorui.agentapplicationcreator.agent.subagent.model.dto;

import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.FilePatch;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.Issue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
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
public class CodeOptimizationResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -431864448331219504L;

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


    /*
        {
          "summary": "当前项目结构基本合理，但组件与业务逻辑耦合过高",
          "issues": [
            {
              "level": "WARN",
              "type": "ARCHITECTURE",
              "path": "src/components/Timer.vue",
              "message": "计时逻辑与UI混在同一个组件中，不利于复用"
            }
          ],
          "suggestedDiff": [
            {
              "path": "src/composables/useTimer.ts",
              "action": "add",
              "content": "export function useTimer() { ... }"
            },
            {
              "path": "src/components/Timer.vue",
              "action": "modify",
              "content": "<script setup>import {useTimer} ...</script>"
            }
          ],
          "newPatterns": [
            "Vue 项目中将业务逻辑抽到 composables 提升可维护性"
          ],
          "confidence": 0.87
        }

     */


}
