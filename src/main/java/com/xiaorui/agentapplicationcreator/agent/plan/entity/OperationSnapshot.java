package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

/**
 * @description: 单次操作执行前快照，用于失败回滚
 * @author: xiaorui
 * @date: 2026-03-26 20:50
 **/
@Data
@AllArgsConstructor
public class OperationSnapshot {

    /**
     * 原路径执行前是否存在
     */
    private boolean sourceExisted;

    /**
     * 原路径执行前内容，仅文件使用
     */
    private String sourceContent;

    /**
     * 目标路径执行前是否存在，MOVE / RENAME 使用
     */
    private boolean targetExisted;

    /**
     * 目标路径执行前内容，仅文件使用
     */
    private String targetContent;

    /**
     * 原路径
     */
    private Path sourcePath;

    /**
     * 目标路径
     */
    private Path targetPath;
}
