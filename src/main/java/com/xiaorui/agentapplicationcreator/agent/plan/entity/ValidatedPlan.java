package com.xiaorui.agentapplicationcreator.agent.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

/**
 * @description: 已验证的计划
 * @author: xiaorui
 * @date: 2026-01-05 20:55
 **/

@Data
@AllArgsConstructor
public class ValidatedPlan {

    /**
     * 根目录
     */
    private Path rootDir;

    /**
     * 已验证的操作
     */
    private List<ValidatedOperation> operations;

    /**
     * 验证计划
     */
    private VerificationPlan verification;
}

