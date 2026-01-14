package com.xiaorui.agentapplicationcreator.agent.subagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizationRun;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizationRunService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.CodeOptimizationRunMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码优化审计主表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class CodeOptimizationRunServiceImpl extends ServiceImpl<CodeOptimizationRunMapper, CodeOptimizationRun>  implements CodeOptimizationRunService {

    /**
     * 保存代码优化审计记录
     *
     * @param codeOptimizationInput  代码优化输入
     * @param codeOptimizationResult 代码优化结果
     */
    @Override
    public void saveCodeOptimizationRun(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult) {
        ThrowUtil.throwIf(codeOptimizationInput == null, ErrorCode.PARAMS_ERROR, "代码优化输入为空");
        ThrowUtil.throwIf(StrUtil.isBlank(codeOptimizationInput.getAppId()), ErrorCode.PARAMS_ERROR, "应用id为空");
        ThrowUtil.throwIf(codeOptimizationResult == null, ErrorCode.PARAMS_ERROR, "代码优化结果为空");
        String appId = codeOptimizationInput.getAppId();
        String appGoal = codeOptimizationInput.getAppGoal();
        List<String> techStack = codeOptimizationInput.getTechStack();
        String summary = codeOptimizationResult.getSummary();
        CodeOptimizationRun codeOptimizationRun = new CodeOptimizationRun();
        codeOptimizationRun.setAppId(appId);
        codeOptimizationRun.setAppGoal(appGoal);
        codeOptimizationRun.setTechStack(techStack.toString());
        codeOptimizationRun.setSummary(summary);
        boolean result = this.save(codeOptimizationRun);
        if (!result) {
            log.error("保存代码优化审计记录失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        }
    }
}
