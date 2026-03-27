package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.modelcall.ModelCallLogQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ModelCallLog;
import com.xiaorui.agentapplicationcreator.model.vo.ModelCallStatsVO;
import org.springframework.scheduling.annotation.Async;

public interface ModelCallLogService extends IService<ModelCallLog> {

    QueryWrapper getQueryWrapper(ModelCallLogQueryRequest queryRequest);

    ModelCallStatsVO getStats();

    void record(ModelCallLog modelCallLog);

    @Async("agentPersistExecutor")
    void persistAsync(ModelCallLog modelCallLog);
}
