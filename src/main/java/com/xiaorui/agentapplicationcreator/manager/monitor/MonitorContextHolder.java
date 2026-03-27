package com.xiaorui.agentapplicationcreator.manager.monitor;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 监控上下文持有者
 * @author: xiaorui
 * @date: 2026-01-23 10:19
 **/
@Slf4j
public class MonitorContextHolder {

    /**
     * 使用 TransmittableThreadLocal 来传递监控上下文，避免线程池中线程的上下文丢失
     */
    private static final TransmittableThreadLocal<MonitorContext> CONTEXT_HOLDER =
            new TransmittableThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前监控上下文
     */
    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

}
