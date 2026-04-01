package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.minio.MinioManager;
import com.xiaorui.agentapplicationcreator.service.ScreenshotService;
import com.xiaorui.agentapplicationcreator.util.WebScreenshotUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author xiaorui
 */
@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private MinioManager minioManager;

    @Resource
    private AppProperties appProperties;

    @Override
    public String createAndUploadScreenshot(String webUrl) {
        ThrowUtil.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "webUrl is blank");
        ThrowUtil.throwIf(!isAllowedScreenshotUrl(webUrl), ErrorCode.PARAMS_ERROR, "screenshot url is not allowed");
        log.info("start creating screenshot, url: {}", webUrl);
        String localScreenshotPath = WebScreenshotUtil.saveWebPageScreenshot(webUrl);
        ThrowUtil.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "failed to create local screenshot");
        try {
            String minioUrl = uploadScreenshotToMinio(localScreenshotPath);
            ThrowUtil.throwIf(StrUtil.isBlank(minioUrl), ErrorCode.OPERATION_ERROR, "failed to upload screenshot");
            log.info("screenshot created and uploaded: {} -> {}", webUrl, minioUrl);
            return minioUrl;
        } finally {
            cleanupLocalFile(localScreenshotPath);
        }
    }

    private String uploadScreenshotToMinio(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("screenshot file does not exist: {}", localScreenshotPath);
            return null;
        }
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String minioFileName = generateScreenshotKey(fileName);
        return minioManager.uploadFile(screenshotFile, minioFileName);
    }

    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    private void cleanupLocalFile(String localFilePath) {
        if (StrUtil.isBlank(localFilePath)) {
            return;
        }
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("cleaned local screenshot file: {}", localFilePath);
        }
    }

    private boolean isAllowedScreenshotUrl(String webUrl) {
        try {
            URI requestUri = URI.create(webUrl);
            URI baseUri = URI.create(appProperties.getDeploy().getPublicBaseUrl());
            if (StrUtil.isBlank(requestUri.getScheme()) || StrUtil.isBlank(requestUri.getHost())) {
                return false;
            }
            if (!requestUri.getScheme().equalsIgnoreCase(baseUri.getScheme())) {
                return false;
            }
            if (!requestUri.getHost().equalsIgnoreCase(baseUri.getHost())) {
                return false;
            }
            return normalizePort(requestUri.getScheme(), requestUri.getPort())
                    == normalizePort(baseUri.getScheme(), baseUri.getPort());
        } catch (Exception e) {
            log.warn("invalid screenshot url: {}", webUrl, e);
            return false;
        }
    }

    private int normalizePort(String scheme, int port) {
        if (port > 0) {
            return port;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return 80;
    }
}
