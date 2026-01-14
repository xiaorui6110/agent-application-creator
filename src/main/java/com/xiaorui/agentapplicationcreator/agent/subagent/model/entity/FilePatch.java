package com.xiaorui.agentapplicationcreator.agent.subagent.model.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 文件修改
 * @author: xiaorui
 * @date: 2026-01-13 16:38
 **/

@Data
public class FilePatch implements Serializable {

    @Serial
    private static final long serialVersionUID = -2138162405985422472L;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 操作类型，add / modify / delete
     */
    private String action;

    /**
     * 文件内容
     */
    private String content;
}
