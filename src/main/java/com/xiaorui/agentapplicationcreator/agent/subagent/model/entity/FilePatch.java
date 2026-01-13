package com.xiaorui.agentapplicationcreator.agent.subagent.model.entity;

import lombok.Data;

/**
 * @description: 文件补丁
 * @author: xiaorui
 * @date: 2026-01-13 16:38
 **/

@Data
public class FilePatch {

    /**
     * 文件路径
     */
    private String path;

    /**
     * 操作类型， add / modify / delete
     */
    private String action;

    /**
     * 文件内容
     */
    private String content;
}
