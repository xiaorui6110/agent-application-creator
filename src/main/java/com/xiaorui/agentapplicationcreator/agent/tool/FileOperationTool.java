package com.xiaorui.agentapplicationcreator.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 文件操作工具（方法工具 methodTools）
 * @author: xiaorui
 * @date: 2026-01-04 22:04
 **/

public class FileOperationTool {

    /**
     * 1. 创建单个文件（若文件已存在则跳过，避免覆盖）
     */
    @Tool(description = "Create a single file at the specified path, skip creation if the file already exists")
    public String createFile(
            @ToolParam(description = "Full path of the file to be created, e.g. D:/test.txt or /usr/local/app/data.log") String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return "File creation failed: The file [" + filePath + "] already exists";
            }
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            boolean isCreated = file.createNewFile();
            return isCreated ? "File created successfully: [" + filePath + "]"
                    : "File creation failed: Unknown error";
        } catch (Exception e) {
            return "File creation exception: " + e.getMessage();
        }
    }

    /**
     * 2. 读取单个文件的内容（支持常规文本文件）
     */
    @Tool(description = "Read the content of a single text file, return the file content directly")
    public String readFile(
            @ToolParam(description = "Full path of the file to be read, e.g. D:/test.txt or /usr/local/app/data.log") String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "File reading failed: The file [" + filePath + "] does not exist";
            }
            if (!file.isFile()) {
                return "File reading failed: [" + filePath + "] is a directory, not a file";
            }
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            return "File content reading successful:\n" + content;
        } catch (Exception e) {
            return "File reading exception: " + e.getMessage();
        }
    }

    /**
     * 3. 修改单个文件内容（覆盖式写入，支持新增/替换文件内容）
     */
    @Tool(description = "Modify the content of a single file, overwrite the original content with new content")
    public String modifyFile(
            @ToolParam(description = "Full path of the file to be modified, e.g. D:/test.txt") String filePath,
            @ToolParam(description = "New content to be written into the file") String newContent) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "File modification failed: The file [" + filePath + "] does not exist";
            }
            if (!file.isFile()) {
                return "File modification failed: [" + filePath + "] is a directory";
            }
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                bw.write(newContent);
            }
            return "File modified successfully: [" + filePath + "]";
        } catch (Exception e) {
            return "File modification exception: " + e.getMessage();
        }
    }

    /**
     * 4. 删除单个文件（仅删除文件，不删除目录）
     */
    @Tool(description = "Delete a single file, only delete files and do not support directory deletion")
    public String deleteFile(
            @ToolParam(description = "Full path of the file to be deleted, e.g. D:/test.txt or /usr/local/app/data.log") String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "File deletion failed: The file [" + filePath + "] does not exist";
            }
            if (!file.isFile()) {
                return "File deletion failed: [" + filePath + "] is a directory, this method does not support directory deletion";
            }
            boolean isDeleted = file.delete();
            return isDeleted ? "File deleted successfully: [" + filePath + "]"
                    : "File deletion failed: Unknown error";
        } catch (Exception e) {
            return "File deletion exception: " + e.getMessage();
        }
    }

    /**
     * 5. 递归获取指定目录下的所有文件结构（包含子目录+所有层级文件）
     */
    @Tool(description = "Recursively get the complete file structure of the specified directory, including all subdirectories and all levels of files under the directory")
    public String listDirFilesRecursively(
            @ToolParam(description = "Full path of the target directory, e.g. D:/testDir or /usr/local/app") String dirPath) {
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                return "Directory traversal failed: The directory [" + dirPath + "] does not exist";
            }
            if (!dir.isDirectory()) {
                return "Directory traversal failed: [" + dirPath + "] is a file, not a directory";
            }
            List<String> fileStructure = new ArrayList<>();
            fileStructure.add("=== Directory full structure: " + dirPath + " ===");
            collectFileStructure(dir, fileStructure, 0);

            StringBuilder result = new StringBuilder();
            for (String line : fileStructure) {
                result.append(line).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Directory traversal exception: " + e.getMessage();
        }
    }

    /**
     * 6. 删除指定的空目录（仅删除空目录，非空目录拒绝删除，保障数据安全）
     */
    @Tool(description = "Delete the specified empty directory, refuse to delete non-empty directories to ensure data security")
    public String deleteEmptyDir(
            @ToolParam(description = "Full path of the empty directory to be deleted, e.g. D:/testDir or /usr/local/app/temp") String dirPath) {
        try {
            File dir = new File(dirPath);
            // 校验目录是否存在
            if (!dir.exists()) {
                return "Directory deletion failed: The directory [" + dirPath + "] does not exist";
            }
            // 校验是否为目录（非文件）
            if (!dir.isDirectory()) {
                return "Directory deletion failed: [" + dirPath + "] is a file, not a directory";
            }
            // 校验目录是否为空（核心校验，防止误删非空目录）
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                return "Directory deletion failed: The directory [" + dirPath + "] is not empty, please clear the files first";
            }
            // 执行空目录删除
            boolean isDeleted = dir.delete();
            return isDeleted ? "Empty directory deleted successfully: [" + dirPath + "]"
                    : "Empty directory deletion failed: Unknown error";
        } catch (Exception e) {
            return "Directory deletion exception: " + e.getMessage();
        }
    }

    /**
     * 7. 文件重命名 / 移动（2合1功能，同路径=重命名，不同路径=移动+重命名）
     */
    @Tool(description = "Rename or move a single file, same path means rename, different path means move and rename")
    public String renameOrMoveFile(
            @ToolParam(description = "Original full path of the file, e.g. D:/old.txt or /usr/local/app/old.log") String oldFilePath,
            @ToolParam(description = "New full path of the file, e.g. D:/new.txt or /usr/local/newDir/new.log") String newFilePath) {
        try {
            File oldFile = new File(oldFilePath);
            File newFile = new File(newFilePath);

            // 原文件校验
            if (!oldFile.exists()) {
                return "File operation failed: The original file [" + oldFilePath + "] does not exist";
            }
            if (!oldFile.isFile()) {
                return "File operation failed: [" + oldFilePath + "] is a directory, this method only supports file operation";
            }
            // 新文件路径-自动创建父目录（移动文件时生效）
            File newFileParent = newFile.getParentFile();
            if (newFileParent != null && !newFileParent.exists()) {
                newFileParent.mkdirs();
            }
            // 新文件已存在则提示（避免覆盖）
            if (newFile.exists()) {
                return "File operation failed: The target file [" + newFilePath + "] already exists, cannot overwrite";
            }
            // 执行重命名/移动操作
            boolean isSuccess = oldFile.renameTo(newFile);
            return isSuccess ? "File operation successful: [" + oldFilePath + "] -> [" + newFilePath + "]"
                    : "File rename/move failed: Unknown error (please check file permissions)";
        } catch (Exception e) {
            return "File rename/move exception: " + e.getMessage();
        }
    }

    /**
     * 私有辅助方法：递归收集文件结构，实现层级缩进展示
     */
    private void collectFileStructure(File file, List<String> structure, int level) {
        String indent = "  ".repeat(level);
        if (file.isDirectory()) {
            structure.add(indent + "📂 " + file.getName() + "/");
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectFileStructure(child, structure, level + 1);
                }
            }
        } else {
            structure.add(indent + "📄 " + file.getName());
        }
    }

}
