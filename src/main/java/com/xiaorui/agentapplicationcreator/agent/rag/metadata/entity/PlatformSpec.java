package com.xiaorui.agentapplicationcreator.agent.rag.metadata.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 平台规范
 * @author: xiaorui
 * @date: 2026-01-17 20:56
 **/
@Data
public class PlatformSpec implements Serializable {

    @Serial
    private static final long serialVersionUID = 7286375774951957964L;

    /**
     * 规范ID
     */
    String specId;

    /**
     * spec markdown 原文
     */
    String content;

    /**
     * 规范元信息
     */
    SpecMetadata metadata;

}
