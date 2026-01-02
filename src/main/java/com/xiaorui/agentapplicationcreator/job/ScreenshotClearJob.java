package com.xiaorui.agentapplicationcreator.job;

import com.xiaorui.agentapplicationcreator.util.WebScreenshotUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description: 定时清理过期的临时截图文件
 * @author: xiaorui
 * @date: 2026-01-02 15:21
 **/
@Slf4j
@Component
@EnableScheduling
public class ScreenshotClearJob {

    /**
     * 每天凌晨2点清理过期的临时截图文件
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempScreenshots() {
        log.info("开始定时清理过期的临时截图文件");
        try {
            WebScreenshotUtil.cleanupTempFiles();
            log.info("定时清理临时截图文件完成");
        } catch (Exception e) {
            log.error("定时清理临时截图文件失败", e);
        }
    }

}
