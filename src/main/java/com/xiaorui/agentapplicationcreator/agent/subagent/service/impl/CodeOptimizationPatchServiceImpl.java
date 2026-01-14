package com.xiaorui.agentapplicationcreator.agent.subagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizationPatch;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.FilePatch;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizationPatchService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.CodeOptimizationPatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码优化修改建议表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class CodeOptimizationPatchServiceImpl extends ServiceImpl<CodeOptimizationPatchMapper, CodeOptimizationPatch>  implements CodeOptimizationPatchService{

    /**
     * 保存代码优化修改建议
     *
     * @param codeOptimizationInput  代码优化输入
     * @param codeOptimizationResult 代码优化结果
     */
    @Override
    public void saveCodeOptimizationPatch(CodeOptimizationInput codeOptimizationInput, CodeOptimizationResult codeOptimizationResult) {
        ThrowUtil.throwIf(codeOptimizationInput == null, ErrorCode.PARAMS_ERROR, "代码优化输入为空");
        ThrowUtil.throwIf(StrUtil.isBlank(codeOptimizationInput.getAppId()), ErrorCode.PARAMS_ERROR, "应用id为空");
        ThrowUtil.throwIf(codeOptimizationResult == null, ErrorCode.PARAMS_ERROR, "代码优化结果为空");
        String appId = codeOptimizationInput.getAppId();
        List<FilePatch> suggestedDiff = codeOptimizationResult.getSuggestedDiff();
        List<String> allPath = suggestedDiff.stream().map(FilePatch::getPath).toList();
        List<String> allAction = suggestedDiff.stream().map(FilePatch::getAction).toList();
        List<String> allContent = suggestedDiff.stream().map(FilePatch::getContent).toList();
        CodeOptimizationPatch codeOptimizationPatch = new CodeOptimizationPatch();
        codeOptimizationPatch.setAppId(appId);
        codeOptimizationPatch.setPatchId(allPath.toString());
        codeOptimizationPatch.setAction(allAction.toString());
        codeOptimizationPatch.setContent(allContent.toString());
        boolean result = this.save(codeOptimizationPatch);
        if (!result) {
            log.error("保存代码优化修改建议失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        }

    }
}
