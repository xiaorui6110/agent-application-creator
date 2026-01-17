package com.xiaorui.agentapplicationcreator.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

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
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
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

    @NotNull
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            // 可以发邮件、记录日志等
            System.err.println("异步方法异常: " + method.getName() + " - " + ex.getMessage());
        };
    }

}
