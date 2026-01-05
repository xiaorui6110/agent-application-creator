package com.xiaorui.agentapplicationcreator.agent.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 工具调用结果（GPT 优化）
 * @author: xiaorui
 * @date: 2025-12-15 15:48
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallResponse {

    /** 工具名称 */
    private String toolName;

    /** 执行的动作语义（read_file / overwrite_file / list_dir） */
    private String action;

    /** 操作目标（相对路径） */
    private String target;

    /** 工具输入参数 */
    private Object input;

    /** 工具原始输出（结构化数据，而不是描述性文本） */
    private Object result;

    /** Tool 是否在技术层面执行成功（无异常） */
    private boolean invokedSuccessfully;

    /** 结果是否已被验证（例如是否 readFile 确认） */
    private boolean verified;

    /** 若未验证，给 Agent 的下一步建议 */
    private String nextActionHint;

    /** 执行耗时 */
    private Long costMs;
}

