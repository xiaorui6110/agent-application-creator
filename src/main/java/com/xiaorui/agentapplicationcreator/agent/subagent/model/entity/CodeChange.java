package com.xiaorui.agentapplicationcreator.agent.subagent.model.entity;

import lombok.Data;

/**
 * @description: 代码变化
 * @author: xiaorui
 * @date: 2026-01-13 16:36
 **/
@Data
public class CodeChange {

    /**
     * 变化代码路径
     */
    private String path;

    /**
     * 操作类型， add / modify / delete
     */
    private String type;

    /**
     * 变化代码
     */
    private String diff;
}
