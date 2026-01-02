package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.minio.MinioManager;
import com.xiaorui.agentapplicationcreator.service.ScreenshotService;
import com.xiaorui.agentapplicationcreator.util.WebScreenshotUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @description: 截图服务实现类（在应用部署完成后触发截图生成）
 * @author: xiaorui
 * @date: 2026-01-02 16:02
 **/
@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private MinioManager minioManager;

    /**
     * 通用的截图服务，可以得到访问地址
     *
     * @param webUrl 网址
     * @return 截图访问地址
     */
    @Override
    public String createAndUploadScreenshot(String webUrl) {
        ThrowUtil.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);
        // 生成本地截图
        String localScreenshotPath = WebScreenshotUtil.saveWebPageScreenshot(webUrl);
        ThrowUtil.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        try {
            // 上传到存储桶
            String minioUrl = uploadScreenshotToMinio(localScreenshotPath);
            ThrowUtil.throwIf(StrUtil.isBlank(minioUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, minioUrl);
            return minioUrl;
        } finally {
            // 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }


    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToMinio(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成文件名并上传文件
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String minioFileName = generateScreenshotKey(fileName);
        return minioManager.uploadFile(screenshotFile, minioFileName);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2026_01_02/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
