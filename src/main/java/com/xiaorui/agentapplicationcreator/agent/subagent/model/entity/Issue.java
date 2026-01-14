package com.xiaorui.agentapplicationcreator.agent.subagent.model.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 代码问题
 * @author: xiaorui
 * @date: 2026-01-13 16:38
 **/
@Data
public class Issue implements Serializable {

    @Serial
    private static final long serialVersionUID = 3054081259061639064L;

    /**
     * 问题级别，INFO / WARN / ERROR
     */
    private String level;

    /**
     * 问题类型，ARCHITECTURE / STYLE / BUG / SMELL
     */
    private String type;

    /**
     * 问题路径
     */
    private String path;

    /**
     * 问题信息
     */
    private String message;

}
