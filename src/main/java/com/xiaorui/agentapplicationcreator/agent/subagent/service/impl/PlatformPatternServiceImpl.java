package com.xiaorui.agentapplicationcreator.agent.subagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.PlatformPattern;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.PlatformPatternService;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.PlatformPatternMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 代码优化平台级经验表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class PlatformPatternServiceImpl extends ServiceImpl<PlatformPatternMapper, PlatformPattern>  implements PlatformPatternService{

    /**
     * 保存代码优化平台级经验
     *
     * @param patternText 平台级经验
     */
    @Override
    public void savePlatformPattern(String patternText) {
        ThrowUtil.throwIf(StrUtil.isBlank(patternText), ErrorCode.PARAMS_ERROR, "输入参数不能为空");
        PlatformPattern platformPattern = new PlatformPattern();
        platformPattern.setPatternText(patternText);
        platformPattern.setHitCount(platformPattern.getHitCount() + 1);
        platformPattern.setLastSeenAt(LocalDateTime.now());
        boolean result = this.save(platformPattern);
        if (!result) {
            log.error("保存代码优化平台级经验失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        }
    }
}
