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
import java.util.Map;

/**
 * @author xiaorui
 */
@Component
public class CodeFileSaverUtil {

    @Resource
    private AppService appService;

    @Resource
    private AppProperties appProperties;

    public void writeFilesToLocal(Map<String, String> files, String appId) throws IOException {
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
            throw new IOException("generated files is empty");
        }
        FileUtil.mkdir(rootDir.toFile());
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String relativePath = entry.getKey();
            if (StrUtil.isBlank(relativePath)) {
                throw new IOException("file path is blank");
            }
            Path normalizedPath;
            try {
                normalizedPath = appProperties.resolvePathWithinRoot(rootDir, relativePath);
            } catch (IllegalArgumentException e) {
                throw new IOException("illegal file path: " + relativePath, e);
            }
            Path parent = normalizedPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    normalizedPath,
                    entry.getValue() == null ? "" : entry.getValue(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }
    }
}