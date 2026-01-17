package com.xiaorui.agentapplicationcreator.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.*;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * @description: 文件操作工具（方法工具 methodTools） TODO 我相信是可以的，应该是 prompt 没有写好、权限不够等等其他原因导致的，路劲问题之类的
 * @author: xiaorui
 * @date: 2026-01-04 22:04
 **/
//@Component
public class FileOperationTool {

    /**
     * 文件修改根目录
     */
    private static final Path ROOT_DIR = Paths.get(CODE_OUTPUT_ROOT_DIR).toAbsolutePath().normalize();

    /**
     * 将相对路径解析为安全的绝对路径（不抛异常）
     */
    private Path resolveSafePathQuietly(String relativePath) {
        try {
            Path resolved = ROOT_DIR.resolve(relativePath).normalize();
            if (!resolved.startsWith(ROOT_DIR)) {
                return null;
            }
            return resolved;
        } catch (Exception e) {
            return null;
        }
    }

    /* ========================= 创建文件 ========================= */

    @Tool(description = "在 code_output 目录下创建文件并写入内容，如果存在则覆盖")
    public String createFile(
            @ToolParam(description = "相对文件路径（基于 code_output）") String path,
            @ToolParam(description = "文件内容") String content) {

        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "非法路径，禁止访问 code_output 目录之外的文件";
        }

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(
                    filePath,
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            return "文件创建成功：" + path;
        } catch (IOException e) {
            return "创建文件失败：" + e.getMessage();
        }
    }

    /* ========================= 覆盖文件 ========================= */

    @Tool(description = "覆盖 code_output 目录下已有文件的内容")
    public String overwriteFile(
            @ToolParam(description = "相对文件路径（基于 code_output）") String path,
            @ToolParam(description = "新文件内容") String newContent) {

        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "非法路径";
        }

        if (!Files.exists(filePath)) {
            return "文件不存在：" + path;
        }

        if (!Files.isRegularFile(filePath)) {
            return "目标不是普通文件：" + path;
        }

        try {
            Files.writeString(filePath, newContent, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("[Agent Write File] " + filePath.toAbsolutePath());
            return "文件更新成功：" + filePath.toAbsolutePath();
        } catch (IOException e) {
            return "更新文件失败：" + e.getMessage();
        }
    }

    /* ========================= 删除文件 ========================= */

    @Tool(description = "删除 code_output 目录下的文件")
    public String deleteFile(
            @ToolParam(description = "相对文件路径（基于 code_output）") String path) {

        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "非法路径";
        }

        if (!Files.exists(filePath)) {
            return "文件不存在：" + path;
        }

        try {
            Files.delete(filePath);
            return "文件已删除：" + path;
        } catch (IOException e) {
            return "删除文件失败：" + e.getMessage();
        }
    }

    /* ========================= 目录结构 ========================= */

    @Tool(description = "递归列出 code_output 目录下的文件结构")
    public String listDirectoryTree(
            @ToolParam(description = "相对目录路径（基于 code_output，可为空）") String rootPath) {

        Path root = resolveSafePathQuietly(
                rootPath == null || rootPath.isBlank() ? "" : rootPath
        );
        if (root == null) {
            return "非法路径";
        }

        if (!Files.exists(root)) {
            return "目录不存在：" + rootPath;
        }

        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(root).forEach(p ->
                    sb.append(ROOT_DIR.relativize(p)).append("\n")
            );
            return sb.toString();
        } catch (IOException e) {
            return "获取目录结构失败：" + e.getMessage();
        }
    }

    /* ========================= 删除空目录 ========================= */

    @Tool(description = "删除 code_output 目录下的空目录")
    public String deleteEmptyDir(
            @ToolParam(description = "相对目录路径（基于 code_output）") String dirPath) {

        Path dir = resolveSafePathQuietly(dirPath);
        if (dir == null) {
            return "非法路径";
        }

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return "目录不存在：" + dirPath;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            if (stream.iterator().hasNext()) {
                return "目录非空，未删除：" + dirPath;
            }
            Files.delete(dir);
            return "空目录已删除：" + dirPath;
        } catch (IOException e) {
            return "删除目录失败：" + e.getMessage();
        }
    }

    /* ========================= 移动 / 重命名 ========================= */

    @Tool(description = "在 code_output 目录下移动或重命名文件")
    public String moveOrRename(
            @ToolParam(description = "源文件相对路径") String source,
            @ToolParam(description = "目标文件相对路径") String target) {

        Path src = resolveSafePathQuietly(source);
        Path tgt = resolveSafePathQuietly(target);

        if (src == null || tgt == null) {
            return "非法路径";
        }

        if (!Files.exists(src)) {
            return "源文件不存在：" + source;
        }

        try {
            if (tgt.getParent() != null) {
                Files.createDirectories(tgt.getParent());
            }
            Files.move(src, tgt, StandardCopyOption.REPLACE_EXISTING);
            return "文件已移动：" + source + " -> " + target;
        } catch (IOException e) {
            return "移动文件失败：" + e.getMessage();
        }
    }

    /* ========================= 读取文件 ========================= */

    @Tool(description = "读取 code_output 目录下的文件内容")
    public String readFile(
            @ToolParam(description = "相对文件路径（基于 code_output）") String path) {

        Path filePath = resolveSafePathQuietly(path);
        if (filePath == null) {
            return "非法路径";
        }

        if (!Files.exists(filePath)) {
            return "文件不存在：" + path;
        }

        if (!Files.isRegularFile(filePath)) {
            return "目标不是普通文件：" + path;
        }

        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return "读取文件失败：" + e.getMessage();
        }
    }

    /* ========================= 是否存在 ========================= */

    @Tool(description = "检查 code_output 目录下文件或目录是否存在")
    public String exists(
            @ToolParam(description = "相对路径（基于 code_output）") String path) {

        Path p = resolveSafePathQuietly(path);
        if (p == null) {
            return "ILLEGAL_PATH";
        }
        return Files.exists(p) ? "EXISTS" : "NOT_EXISTS";
    }

    /* ========================= 搜索 ========================= */

    @Tool(description = "在 code_output 目录下按名称关键词搜索文件")
    public String searchByName(
            @ToolParam(description = "相对目录路径（基于 code_output，可为空）") String root,
            @ToolParam(description = "关键词") String keyword) {

        Path rootPath = resolveSafePathQuietly(
                root == null || root.isBlank() ? "" : root
        );
        if (rootPath == null) {
            return "非法路径";
        }

        if (!Files.exists(rootPath)) {
            return "目录不存在：" + root;
        }

        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(rootPath)
                    .filter(p -> p.getFileName().toString().contains(keyword))
                    .forEach(p -> sb.append(ROOT_DIR.relativize(p)).append("\n"));

            return sb.length() == 0
                    ? "未找到匹配文件"
                    : sb.toString();
        } catch (IOException e) {
            return "搜索失败：" + e.getMessage();
        }
    }
}

