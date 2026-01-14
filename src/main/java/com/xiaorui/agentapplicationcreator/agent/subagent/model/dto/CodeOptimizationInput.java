package com.xiaorui.agentapplicationcreator.agent.subagent.model.dto;

import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeChange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
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
public class CodeOptimizationInput implements Serializable {

    @Serial
    private static final long serialVersionUID = 3376824323251970444L;

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
     * 平台的长期记忆
     */
    private List<String> platformMemory;


    /*
        {
          "projectId": "abc123",
          "techStack": ["Vue3", "Vite", "TypeScript"],
          "appGoal": "番茄计时专注小工具",
          "fileTree": [
            "src/main.ts",
            "src/App.vue",
            "src/components/Timer.vue"
          ],
          "files": {
            "src/App.vue": "<template>...</template>",
            "src/components/Timer.vue": "..."
          },
          "recentChanges": [
            {
              "path": "src/components/Timer.vue",
              "type": "modified",
              "diff": "..."
            }
          ],
          "platformMemory": [
            "路径容易出错",
            "Vue 项目建议使用 composables"
          ]
        }

     */

}

