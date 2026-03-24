package com.xiaorui.agentapplicationcreator.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
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

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_DEPLOY_ROOT_DIR;
import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

@Component
public class CodeFileSaverUtil {

    @Resource
    private AppService appService;

    public static void writeFilesToLocal(Map<String, String> files, String appId) throws IOException {
        writeFiles(files, Path.of(CODE_OUTPUT_ROOT_DIR, appId));
    }

    public void writeFilesToWeb(Map<String, String> files, String appId) throws IOException {
        App app = appService.getById(appId);
        String deployKey = app.getDeployKey();
        writeFiles(files, Path.of(CODE_DEPLOY_ROOT_DIR, deployKey));
    }

    private static void writeFiles(Map<String, String> files, Path rootDir) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IOException("生成文件为空，无法落盘");
        }
        FileUtil.mkdir(rootDir.toFile());
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String relativePath = entry.getKey();
            if (StrUtil.isBlank(relativePath)) {
                throw new IOException("存在空文件路径，无法落盘");
            }
            Path normalizedPath = rootDir.resolve(relativePath).normalize();
            if (!normalizedPath.startsWith(rootDir)) {
                throw new IOException("非法文件路径: " + relativePath);
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
