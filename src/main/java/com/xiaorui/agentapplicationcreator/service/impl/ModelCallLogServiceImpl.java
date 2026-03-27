package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.ModelCallLogMapper;
import com.xiaorui.agentapplicationcreator.model.dto.modelcall.ModelCallLogQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ModelCallLog;
import com.xiaorui.agentapplicationcreator.model.vo.ModelCallStatsVO;
import com.xiaorui.agentapplicationcreator.service.ModelCallLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ModelCallLogServiceImpl extends ServiceImpl<ModelCallLogMapper, ModelCallLog>
        implements ModelCallLogService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public QueryWrapper getQueryWrapper(ModelCallLogQueryRequest queryRequest) {
        ThrowUtil.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("user_id", queryRequest.getUserId(), StrUtil.isNotBlank(queryRequest.getUserId()))
                .eq("app_id", queryRequest.getAppId(), StrUtil.isNotBlank(queryRequest.getAppId()))
                .eq("thread_id", queryRequest.getThreadId(), StrUtil.isNotBlank(queryRequest.getThreadId()))
                .eq("agent_name", queryRequest.getAgentName(), StrUtil.isNotBlank(queryRequest.getAgentName()))
                .eq("model_name", queryRequest.getModelName(), StrUtil.isNotBlank(queryRequest.getModelName()))
                .eq("call_status", queryRequest.getCallStatus(), StrUtil.isNotBlank(queryRequest.getCallStatus()));
        if (StrUtil.isNotBlank(queryRequest.getSortField())) {
            queryWrapper.orderBy(queryRequest.getSortField(), "ascend".equals(queryRequest.getSortOrder()));
        } else {
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    @Override
    public ModelCallStatsVO getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        ModelCallStatsVO statsVO = new ModelCallStatsVO();
        statsVO.setTotalCallCount(this.count(QueryWrapper.create()));
        statsVO.setTodayCallCount(this.count(QueryWrapper.create().ge("create_time", todayStart)));
        statsVO.setSuccessCallCount(this.count(QueryWrapper.create().eq("call_status", "SUCCESS")));
        statsVO.setFailedCallCount(this.count(QueryWrapper.create().eq("call_status", "FAILED")));
        statsVO.setTotalPromptTokens(queryNumber(
                "SELECT COALESCE(SUM(prompt_tokens), 0) FROM xr_model_call_log WHERE is_deleted = 0"));
        statsVO.setTotalCompletionTokens(queryNumber(
                "SELECT COALESCE(SUM(completion_tokens), 0) FROM xr_model_call_log WHERE is_deleted = 0"));
        statsVO.setTotalTokens(queryNumber(
                "SELECT COALESCE(SUM(total_tokens), 0) FROM xr_model_call_log WHERE is_deleted = 0"));
        statsVO.setAvgLatencyMs(queryNumber(
                "SELECT COALESCE(ROUND(AVG(latency_ms), 0), 0) FROM xr_model_call_log WHERE is_deleted = 0"));
        return statsVO;
    }

    @Override
    public void record(ModelCallLog modelCallLog) {
        if (modelCallLog == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        modelCallLog.setCreateTime(now);
        modelCallLog.setUpdateTime(now);
        if (modelCallLog.getPromptTokens() == null) {
            modelCallLog.setPromptTokens(0);
        }
        if (modelCallLog.getCompletionTokens() == null) {
            modelCallLog.setCompletionTokens(0);
        }
        if (modelCallLog.getTotalTokens() == null) {
            modelCallLog.setTotalTokens(modelCallLog.getPromptTokens() + modelCallLog.getCompletionTokens());
        }
        if (modelCallLog.getLatencyMs() == null) {
            modelCallLog.setLatencyMs(0L);
        }
        persistAsync(modelCallLog);
    }

    @Async("agentPersistExecutor")
    @Override
    public void persistAsync(ModelCallLog modelCallLog) {
        try {
            this.save(modelCallLog);
        } catch (Exception e) {
            log.error("persist model call log failed, modelName={}, agentName={}",
                    modelCallLog.getModelName(), modelCallLog.getAgentName(), e);
        }
    }

    private long queryNumber(String sql) {
        Number number = jdbcTemplate.queryForObject(sql, Number.class);
        return number == null ? 0L : number.longValue();
    }
}
