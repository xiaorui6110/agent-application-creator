package com.xiaorui.agentapplicationcreator.config;

import com.xiaorui.agentapplicationcreator.util.RedisCacheUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 应用启动时清理旧缓存配置（AI 生成的）
 *
 * @author xiaorui
 */
@Slf4j
@Configuration
public class CacheCleanupConfig implements ApplicationRunner {

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 清理 good_app_page 缓存（可能存在序列化不兼容的旧数据）
            log.info("clean Redis cache: good_app_page:*");
            redisCacheUtil.deleteByPattern("good_app_page:*");
            log.info("Redis cache cleanup completed");
        } catch (Exception e) {
            log.error("Redis cache cleanup failed", e);
        }
    }
}
