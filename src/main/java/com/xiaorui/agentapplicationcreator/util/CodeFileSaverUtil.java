package com.xiaorui.agentapplicationcreator.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaorui
 */
@Component
public class CodeFileSaverUtil {

    private static final int MAX_FILE_COUNT = 200;
    private static final int MAX_FILE_PATH_LENGTH = 260;
    private static final int MAX_TOTAL_CONTENT_LENGTH = 2 * 1024 * 1024;

    @Resource
    private AppService appService;

    @Resource
    private AppProperties appProperties;

    public void writeFilesToLocal(Map<String, String> files, String appId) throws IOException {
        validateAppExists(appId);
        writeFiles(files, appProperties.resolveCodeOutputAppDir(appId));
    }

    public void writeFilesToWeb(Map<String, String> files, String appId) throws IOException {
        App app = appService.getById(appId);
        if (app == null || StrUtil.isBlank(app.getDeployKey())) {
            throw new IOException("app or deployKey is missing");
        }
        writeFiles(files, appProperties.resolveCodeDeployAppDir(app.getDeployKey()));
    }

    private void writeFiles(Map<String, String> files, Path rootDir) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IOException("generated files are empty");
        }
        if (files.size() > MAX_FILE_COUNT) {
            throw new IOException("generated file count exceeds limit");
        }
        FileUtil.mkdir(rootDir.toFile());
        Set<Path> normalizedPaths = new HashSet<>();
        int totalContentLength = 0;
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String relativePath = entry.getKey();
            if (StrUtil.isBlank(relativePath)) {
                throw new IOException("file path is blank");
            }
            if (relativePath.length() > MAX_FILE_PATH_LENGTH) {
                throw new IOException("file path is too long: " + relativePath);
            }
            Path normalizedPath;
            try {
                normalizedPath = appProperties.resolvePathWithinRoot(rootDir, relativePath);
            } catch (IllegalArgumentException e) {
                throw new IOException("illegal file path: " + relativePath, e);
            }
            if (normalizedPath.equals(rootDir)) {
                throw new IOException("file path resolves to root directory: " + relativePath);
            }
            if (!normalizedPaths.add(normalizedPath)) {
                throw new IOException("duplicate normalized file path: " + relativePath);
            }
            String fileContent = entry.getValue() == null ? "" : entry.getValue();
            totalContentLength += fileContent.length();
            if (totalContentLength > MAX_TOTAL_CONTENT_LENGTH) {
                throw new IOException("generated content size exceeds limit");
            }
            Path parent = normalizedPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    normalizedPath,
                    fileContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }
    }

    private void validateAppExists(String appId) throws IOException {
        if (StrUtil.isBlank(appId)) {
            throw new IOException("appId is blank");
        }
        App app = appService.getById(appId);
        if (app == null) {
            throw new IOException("app not found");
        }
    }
}
