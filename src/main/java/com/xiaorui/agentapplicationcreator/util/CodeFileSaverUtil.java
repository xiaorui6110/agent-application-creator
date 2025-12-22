package com.xiaorui.agentapplicationcreator.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.ai.model.result.MultiFileCodeResult;
import com.xiaorui.agentapplicationcreator.ai.model.result.SingleFileCodeResult;
import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @description: 代码文件保存工具类
 * @author: xiaorui
 * @date: 2025-12-22 14:08
 **/
public class CodeFileSaverUtil {

    /**
     * 文件保存根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/codeoutput";

    /**
     * 保存 SingleFileCodeResult
     */
    public static File saveHtmlCodeResult(SingleFileCodeResult singleFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.SINGLE_FILE.getValue());
        writeToFile(baseDirPath, "index.html", singleFileCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存 MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径：tmp/codeoutput/bizType_雪花ID
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}

