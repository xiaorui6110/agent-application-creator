package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import lombok.Data;

import java.util.List;

/**
 * @description: 代码修改计划
 * @author: xiaorui
 * @date: 2026-01-05 20:43
 **/
@Data
public class CodeModificationPlan {

    /**
     * 固定为 CODE_MODIFICATION
     */
    private String planType = "CODE_MODIFICATION";

    /**
     * 固定为 code_output，用于二次校验
     */
    private String rootDir;

    /**
     * 文件操作计划
     */
    private List<FileOperationPlan> operations;

    /**
     * 验证计划
     */
    private VerificationPlan verification;
}

