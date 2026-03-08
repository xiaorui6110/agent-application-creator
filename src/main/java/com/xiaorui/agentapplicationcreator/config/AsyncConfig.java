package com.xiaorui.agentapplicationcreator.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 异步配置
 *                  CPU密集型任务：核心线程数 = CPU核心数 + 1
 *                  IO密集型任务：核心线程数 = CPU核心数 × 2
 *                  队列容量：一般设置为核心线程数的5-10倍
 * @author: xiaorui
 * @date: 2026-01-15 12:35
 **/
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean("codeOptExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅关闭配置
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setThreadNamePrefix("async-codeOpt-");
        executor.initialize();
        return executor;
    }

    /**
     * 虚拟线程执行器 - 用于异步持久化任务
     * 适用场景：I/O 密集型任务（数据库写入）
     * 优势：按需创建、内存占用小、高并发不阻塞
     */
    @Bean("agentPersistExecutor")
    public Executor agentPersistExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean("appLikeOrShareExecutor")
    public Executor appLikeExecutor() {
        return new ThreadPoolExecutor(
                4,
                8,
                15L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(256),
                new ThreadFactory() {
                    private final AtomicInteger threadNum = new AtomicInteger(1);
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("app-async-like" + threadNum.getAndIncrement());
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @NotNull
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            // 可以发邮件、记录日志等
            System.err.println("异步方法异常: " + method.getName() + " - " + ex.getMessage());
        };
    }

}
