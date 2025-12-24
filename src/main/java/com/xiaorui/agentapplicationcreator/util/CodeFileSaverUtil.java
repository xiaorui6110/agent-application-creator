package com.xiaorui.agentapplicationcreator.util;

import cn.hutool.core.io.FileUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * @description: 代码文件保存工具类（已改为 Hook 实现）
 * @author: xiaorui
 * @date: 2025-12-22 14:08
 **/
@Component
public class CodeFileSaverUtil {

    /**
     * 文件保存根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code";

    /**
     * 将 structuredReply.files 写入本地目录
     *
     * @param files key: 相对路径, value: 文件内容
     * @param appId 应用ID
     */
    public void writeFiles(Map<String, String> files, String appId) throws IOException {
        for (Map.Entry<String, String> entry : files.entrySet()) {

            String relativePath = entry.getKey();
            String content = entry.getValue();

            // 构建唯一目录路径：tmp/code/{appId} ,也方便查找
            Path uniqueDirName = Paths.get(appId);
            String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
            // 创建应用存放文件夹
            FileUtil.mkdir(dirPath);

            Path baseDir = Path.of(dirPath);
            Path targetPath = baseDir.resolve(relativePath);

            // 写文件（会覆盖已写入文件的内容）
            Files.writeString(
                    targetPath,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

        }
    }
}

