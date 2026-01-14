package com.xiaorui.agentapplicationcreator.agent.subagent.model.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 代码变化
 * @author: xiaorui
 * @date: 2026-01-13 16:36
 **/
@Data
public class CodeChange implements Serializable {

    @Serial
    private static final long serialVersionUID = -6377155383865858982L;

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
