package com.xiaorui.agentapplicationcreator.agent.plan.result;

import com.xiaorui.agentapplicationcreator.agent.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

/**
 * @description: 操作结果
 * @author: xiaorui
 * @date: 2026-01-05 21:08
 **/
@Data
@AllArgsConstructor
public class OperationResult {

    /**
     * 操作类型
     */
    private OperationTypeEnum operationType;

    /**
     * 操作路径
     */
    private Path path;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 信息
     */
    private String message;
}
