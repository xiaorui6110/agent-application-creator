package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.modelcall.ModelCallLogQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ModelCallLog;
import com.xiaorui.agentapplicationcreator.model.vo.ModelCallStatsVO;
import org.springframework.scheduling.annotation.Async;

/**
 * @author xiaorui
 */
public interface ModelCallLogService extends IService<ModelCallLog> {

    /**
     * 获取查询条件
     * @param queryRequest 模型调用日志查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ModelCallLogQueryRequest queryRequest);

    /**
     * 获取模型调用统计信息
     * @return 模型调用统计信息
     */
    ModelCallStatsVO getStats();

    /**
     * 记录模型调用日志
     * @param modelCallLog 模型调用日志
     */
    void record(ModelCallLog modelCallLog);

    /**
     * 异步记录模型调用日志
     * @param modelCallLog 模型调用日志
     */
    @Async("agentPersistExecutor")
    void persistAsync(ModelCallLog modelCallLog);
}
