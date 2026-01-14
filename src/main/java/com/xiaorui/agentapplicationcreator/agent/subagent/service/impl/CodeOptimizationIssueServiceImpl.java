package com.xiaorui.agentapplicationcreator.agent.subagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizationIssue;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.Issue;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizationIssueService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.CodeOptimizationIssueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码优化问题清单表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class CodeOptimizationIssueServiceImpl extends ServiceImpl<CodeOptimizationIssueMapper, CodeOptimizationIssue>  implements CodeOptimizationIssueService{

    /**
     * 保存代码优化问题清单
     *
     * @param codeOptimizationInput  代码优化输入
     * @param codeOptimizationResult 代码优化结果
     */
    @Override
    public void saveCodeOptimizationIssue(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult) {
        ThrowUtil.throwIf(codeOptimizationInput == null, ErrorCode.PARAMS_ERROR, "代码优化输入为空");
        ThrowUtil.throwIf(StrUtil.isBlank(codeOptimizationInput.getAppId()), ErrorCode.PARAMS_ERROR, "应用id为空");
        ThrowUtil.throwIf(codeOptimizationResult == null, ErrorCode.PARAMS_ERROR, "代码优化结果为空");
        String appId = codeOptimizationInput.getAppId();
        List<Issue> issues = codeOptimizationResult.getIssues();
        List<String> allLevel = issues.stream().map(Issue::getLevel).toList();
        List<String> allType = issues.stream().map(Issue::getType).toList();
        List<String> allPath = issues.stream().map(Issue::getPath).toList();
        List<String> allMessage = issues.stream().map(Issue::getMessage).toList();
        CodeOptimizationIssue codeOptimizationIssue = new CodeOptimizationIssue();
        codeOptimizationIssue.setAppId(appId);
        codeOptimizationIssue.setLevel(allLevel.toString());
        codeOptimizationIssue.setType(allType.toString());
        codeOptimizationIssue.setPath(allPath.toString());
        codeOptimizationIssue.setMessage(allMessage.toString());
        boolean result = this.save(codeOptimizationIssue);
        if (!result) {
            log.error("保存代码优化问题清单失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        }
    }
}
