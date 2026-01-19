package com.xiaorui.agentapplicationcreator.agent.orchestrator;

import com.xiaorui.agentapplicationcreator.agent.subagent.CodeOptimization;
import com.xiaorui.agentapplicationcreator.agent.subagent.model.dto.CodeOptimizationInput;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: Agent 任务池
 * @author: xiaorui
 * @date: 2026-01-19 18:09
 **/
@Component
@Slf4j
public class AgentTaskExecutor implements DisposableBean {

    /**
     * 任务池（AI 并发场景：CPU 密集型任务 + IO 密集型任务）
     */
    private final ExecutorService executor = new ThreadPoolExecutor(
            16,
            16,
            60L,
            TimeUnit.MILLISECONDS,
            // 有界队列，按需调整
            new LinkedBlockingQueue<>(2048),
            // 自定义线程工厂，线程名带业务标识，日志排查方便
            new ThreadFactory() {
                private final AtomicInteger threadNum = new AtomicInteger(1);
                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("agent-async-thread-" + threadNum.getAndIncrement());
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Resource
    private CodeOptimization codeOptimizationAgent;

    /**
     * 提交任务（主 Agent）
     */
    public void submitAgentTask(String taskId, Runnable task) {
        executor.submit(() -> {
            try {
                log.info("Agent task started, taskId={}", taskId);
                task.run();
            } catch (Exception e) {
                log.error("Agent task failed, taskId={}", taskId, e);
            }
        });
    }

    /**
     * 提交任务（副 Agent）
     */
    public void submitOptimizationTask(CodeOptimizationInput input, String threadId, String appId) {
        executor.submit(() -> {
            try {
                log.info("CodeOptimizationAgent started, threadId={}, appId={}", threadId, appId);

                codeOptimizationAgent.codeOptimizeAsync(input);

            } catch (Exception e) {
                log.error("Optimization agent failed", e);
            }
        });
    }

    /**
     * 关闭任务池
     */
    @Override
    public void destroy() throws Exception {
        // 不再接收新任务，等待队列中任务执行完毕
        executor.shutdown();
        // 可选：超时强制关闭（5分钟）
        //if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
        //    executor.shutdownNow();
        //}
    }
}

