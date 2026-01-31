package com.xiaorui.agentapplicationcreator.manager.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @description: Agent 指标收集器
 * @author: xiaorui
 * @date: 2026-01-23 10:28
 **/
@Slf4j
//@Component
public class AgentMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 缓存已创建的指标，避免重复创建（按指标类型分离缓存）
     * 注：tokenCountersCache 暂时无法使用，待框架后续提供支持
     */
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();

    /**
     * 记录请求次数
     */
    public void recordRequest(String userId, String appId, String agentName, String status) {
        String key = String.format("%s_%s_%s_%s", userId, appId, agentName, status);
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("agent_requests_total")
                        .description("Agent 总请求次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("agent_name", agentName)
                        .tag("status", status)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录错误
     */
    public void recordError(String userId, String appId, String agentName, String errorMessage) {
        String key = String.format("%s_%s_%s_%s", userId, appId, agentName, errorMessage);
        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                Counter.builder("agent_errors_total")
                        .description("Agent 错误次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", agentName)
                        .tag("error_message", errorMessage)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录Token消耗（暂时无法使用，待框架后续提供支持）
     */
    public void recordTokenUsage(String userId, String appId, String agentName, String tokenType, long tokenCount) {
        String key = String.format("%s_%s_%s_%s", userId, appId, agentName, tokenType);
        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("agent_tokens_total")
                        .description("Agent 的 Token 消耗总数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", agentName)
                        .tag("token_type", tokenType)
                        .register(meterRegistry)
        );
        counter.increment(tokenCount);
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(String userId, String appId, String agentName, Duration duration) {
        String key = String.format("%s_%s_%s", userId, appId, agentName);
        Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                Timer.builder("agent_response_duration_seconds")
                        .description("Agent 响应时间")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", agentName)
                        .register(meterRegistry)
        );
        timer.record(duration);
    }

}
