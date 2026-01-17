package com.xiaorui.agentapplicationcreator.agent.rag.metadata.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @description: 规范元信息
 * @author: xiaorui
 * @date: 2026-01-17 20:54
 **/
@Data
public class SpecMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -3195780367328229119L;

    /**
     * 规范ID
     */
    String specId;

    /**
     * SINGLE_FILE / MULTI_FILE / VUE_PROJECT
     */
    String generationMode;

    /**
     * SINGLE_FILE / MULTI_FILE / VUE_PROJECT
     */
    List<String> stage;

    /**
     * 技术栈
     */
    List<String> techStack;

    /**
     * 优先级
     */
    int priority;

    /**
     * 版本
     */
    String version;

    /**
     * ACTIVE / DISABLED
     */
    String status;

    /**
     * 从 document metadata 中构建 SpecMetadata
     */
    public static SpecMetadata from(Map<String, Object> metadata) {

        SpecMetadata spec = new SpecMetadata();

        spec.setSpecId((String) metadata.get("specId"));
        spec.setGenerationMode((String) metadata.get("generationMode"));
        spec.setStage((List<String>) metadata.get("stage"));
        spec.setTechStack((List<String>) metadata.get("techStack"));
        spec.setPriority((Integer) metadata.getOrDefault("priority", 0));
        spec.setVersion((String) metadata.getOrDefault("version", "1.0.0"));
        spec.setStatus((String) metadata.getOrDefault("status", "ACTIVE"));

        return spec;
    }

}
