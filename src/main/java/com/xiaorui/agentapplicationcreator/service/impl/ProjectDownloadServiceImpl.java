package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.service.ProjectDownloadService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Service
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    @Resource
    private AppProperties appProperties;

    @Override
    public boolean downloadProjectAsZip(String appId, HttpServletResponse response) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        File projectDir = appProperties.resolveCodeOutputAppDir(appId).toFile();
        ThrowUtil.throwIf(!projectDir.exists(), ErrorCode.NOT_FOUND_ERROR, "project directory not found");
        ThrowUtil.throwIf(!projectDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "project path is not a directory");
        ThrowUtil.throwIf(isDirectoryEmpty(projectDir), ErrorCode.NOT_FOUND_ERROR, "project files not found");

        String downloadFileName = sanitizeDownloadFileName(appId);
        log.info("start project download, appId: {}, dir: {}", appId, projectDir.getAbsolutePath());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));

        FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
            log.info("project download success, appId: {}", appId);
            return true;
        } catch (IOException e) {
            log.error("project download failed, appId: {}", appId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to package project download");
        }
    }

    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        Path relativePath = projectRoot.relativize(fullPath);
        for (Path part : relativePath) {
            String partName = part.toString();
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            if (IGNORED_EXTENSIONS.stream().anyMatch(ext -> partName.toLowerCase().endsWith(ext))) {
                return false;
            }
        }
        return true;
    }

    private boolean isDirectoryEmpty(File directory) {
        File[] children = directory.listFiles();
        return children == null || children.length == 0;
    }

    private String sanitizeDownloadFileName(String appId) {
        String sanitized = appId.replaceAll("[^A-Za-z0-9_-]", "");
        if (StrUtil.isBlank(sanitized)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid appId for download");
        }
        return sanitized;
    }
}
