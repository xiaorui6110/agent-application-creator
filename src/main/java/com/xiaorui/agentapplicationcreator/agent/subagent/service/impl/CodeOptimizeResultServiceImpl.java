package com.xiaorui.agentapplicationcreator.agent.subagent.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.entity.CodeOptimizeResult;
import com.xiaorui.agentapplicationcreator.agent.subagent.service.CodeOptimizeResultService;
import com.xiaorui.agentapplicationcreator.mapper.CodeOptimizeResultMapper;
import org.springframework.stereotype.Service;

/**
 * 代码优化结果表 服务层实现。
 *
 * @author xiaorui
 */
@Service
public class CodeOptimizeResultServiceImpl extends ServiceImpl<CodeOptimizeResultMapper, CodeOptimizeResult>  implements CodeOptimizeResultService {


    /**
     * 获取代码优化结果
     *
     * @param appId 应用id
     * @return 代码优化结果（查询不到返回 null，不抛异常）
     */
    @Override
    public CodeOptimizeResult getByAppId(String appId) {
        try {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("app_id", appId)
                    .eq("is_deleted", 0)
                    .orderBy("create_time", false)
                    .limit(1);
            return getOne(queryWrapper);
        } catch (Exception e) {
            // 捕获所有查询异常，查询不到数据时，直接返回 null
            return null;
        }
    }
}
