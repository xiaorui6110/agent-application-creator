package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import com.xiaorui.agentapplicationcreator.agent.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

/**
 * @description: 已验证的操作
 * @author: xiaorui
 * @date: 2026-01-05 20:55
 **/
@Data
@AllArgsConstructor
public class ValidatedOperation {

    /**
     * 操作类型
     */
    private OperationTypeEnum operationType;

    /**
     * 已解析、已 normalize、绝对路径
     */
    private Path resolvedPath;

    /**
     * MOVE 专用
     */
    private Path resolvedTargetPath;

    /**
     * 预期条件
     */
    private ExpectedCondition expected;

    /**
     * 写入 / 覆盖内容
     */
    private String content;
}
