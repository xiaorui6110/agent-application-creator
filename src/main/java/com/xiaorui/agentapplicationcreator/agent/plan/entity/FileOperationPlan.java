package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import com.xiaorui.agentapplicationcreator.agent.plan.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description: 文件操作计划（对应的 Operation Schema）
 * @author: xiaorui
 * @date: 2026-01-05 20:37
 **/
@Data
@AllArgsConstructor
public class FileOperationPlan {

    /**
     * 操作类型
     */
    private OperationTypeEnum operationType;

    /**
     * 相对 code_output 的路径
     */
    private String path;

    /**
     * 修改前的约束
     */
    private ExpectedCondition expected;

    /**
     * 写入 / 覆盖内容
     */
    private String content;

    /**
     * MOVE 专用
     */
    private String targetPath;
}
