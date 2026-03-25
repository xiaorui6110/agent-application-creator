package com.xiaorui.agentapplicationcreator.agent.tool;

import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

// @Component
public class FileOperationTool {

    private final AppProperties appProperties;

    public FileOperationTool(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private Path getRootDir() {
        return appProperties.getCodeOutputRootPath();
    }

    private Path resolveSafePathQuietly(String relativePath) {
        try {
            return appProperties.resolvePathWithinRoot(getRootDir(), relativePath);
        } catch (Exception e) {
            return null;
        }
    }

    @Tool(description = "Create or overwrite a file under code_output")
    public String createFile(
            @ToolParam(description = "Relative file path") String path,
            @ToolParam(description = "File content") String content) {

        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "illegal path";
        }

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "created file: " + path;
        } catch (IOException e) {
            return "failed to create file: " + e.getMessage();
        }
    }

    @Tool(description = "Overwrite an existing file under code_output")
    public String overwriteFile(
            @ToolParam(description = "Relative file path") String path,
            @ToolParam(description = "New file content") String newContent) {

        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "illegal path";
        }
        if (!Files.exists(filePath)) {
            return "file does not exist: " + path;
        }
        if (!Files.isRegularFile(filePath)) {
            return "target is not a regular file: " + path;
        }

        try {
            Files.writeString(filePath, newContent, StandardOpenOption.TRUNCATE_EXISTING);
            return "updated file: " + path;
        } catch (IOException e) {
            return "failed to update file: " + e.getMessage();
        }
    }

    @Tool(description = "Delete a file under code_output")
    public String deleteFile(@ToolParam(description = "Relative file path") String path) {
        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "illegal path";
        }
        if (!Files.exists(filePath)) {
            return "file does not exist: " + path;
        }

        try {
            Files.delete(filePath);
            return "deleted file: " + path;
        } catch (IOException e) {
            return "failed to delete file: " + e.getMessage();
        }
    }

    @Tool(description = "List the directory tree under code_output")
    public String listDirectoryTree(@ToolParam(description = "Relative directory path, optional") String rootPath) {
        Path root = resolveSafePathQuietly(rootPath == null || rootPath.isBlank() ? "" : rootPath);
        if (root == null) {
            return "illegal path";
        }
        if (!Files.exists(root)) {
            return "directory does not exist: " + rootPath;
        }

        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(root).forEach(p -> sb.append(getRootDir().relativize(p)).append("\n"));
            return sb.toString();
        } catch (IOException e) {
            return "failed to list directory tree: " + e.getMessage();
        }
    }

    @Tool(description = "Delete an empty directory under code_output")
    public String deleteEmptyDir(@ToolParam(description = "Relative directory path") String dirPath) {
        Path dir = resolveSafePathQuietly(dirPath);
        if (dir == null) {
            return "illegal path";
        }
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return "directory does not exist: " + dirPath;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            if (stream.iterator().hasNext()) {
                return "directory is not empty: " + dirPath;
            }
            Files.delete(dir);
            return "deleted empty directory: " + dirPath;
        } catch (IOException e) {
            return "failed to delete directory: " + e.getMessage();
        }
    }

    @Tool(description = "Move or rename a file under code_output")
    public String moveOrRename(
            @ToolParam(description = "Relative source path") String source,
            @ToolParam(description = "Relative target path") String target) {

        Path src = resolveSafePathQuietly(source);
        Path tgt = resolveSafePathQuietly(target);
        if (src == null || tgt == null) {
            return "illegal path";
        }
        if (!Files.exists(src)) {
            return "source file does not exist: " + source;
        }

        try {
            if (tgt.getParent() != null) {
                Files.createDirectories(tgt.getParent());
            }
            Files.move(src, tgt, StandardCopyOption.REPLACE_EXISTING);
            return "moved file: " + source + " -> " + target;
        } catch (IOException e) {
            return "failed to move file: " + e.getMessage();
        }
    }

    @Tool(description = "Read a file under code_output")
    public String readFile(@ToolParam(description = "Relative file path") String path) {
        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "illegal path";
        }
        if (!Files.exists(filePath)) {
            return "file does not exist: " + path;
        }
        if (!Files.isRegularFile(filePath)) {
            return "target is not a regular file: " + path;
        }

        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return "failed to read file: " + e.getMessage();
        }
    }

    @Tool(description = "Check whether a file or directory exists under code_output")
    public String exists(@ToolParam(description = "Relative path") String path) {
        Path p = resolveSafePathQuietly(path);
        if (p == null) {
            return "ILLEGAL_PATH";
        }
        return Files.exists(p) ? "EXISTS" : "NOT_EXISTS";
    }

    @Tool(description = "Search files by name under code_output")
    public String searchByName(
            @ToolParam(description = "Relative directory path, optional") String root,
            @ToolParam(description = "Keyword") String keyword) {

        Path rootPath = resolveSafePathQuietly(root == null || root.isBlank() ? "" : root);
        if (rootPath == null) {
            return "illegal path";
        }
        if (!Files.exists(rootPath)) {
            return "directory does not exist: " + root;
        }

        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(rootPath)
                    .filter(p -> p.getFileName().toString().contains(keyword))
                    .forEach(p -> sb.append(getRootDir().relativize(p)).append("\n"));
            return sb.length() == 0 ? "no matching file found" : sb.toString();
        } catch (IOException e) {
            return "search failed: " + e.getMessage();
        }
    }
}