package com.xiaorui.agentapplicationcreator.agent.tool;

import com.xiaorui.agentapplicationcreator.agent.model.response.VerifyResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * @description:
 * @author: xiaorui
 * @date: 2026-01-05 16:45
 **/
@Component
public class VerifyFileTool {

    // region

    private static final Path ROOT_DIR = Paths.get(CODE_OUTPUT_ROOT_DIR).toAbsolutePath().normalize();

    private Path resolveSafePath(String relativePath) {
        Path resolved = ROOT_DIR.resolve(relativePath).normalize();
        if (!resolved.startsWith(ROOT_DIR)) {
            throw new IllegalArgumentException("非法路径");
        }
        return resolved;
    }

    /**
     * 验证文件是否存在
     */
    @Tool(description = "验证 code_output 目录下的文件是否存在")
    public VerifyResult verifyExists(
            @ToolParam(description = "相对文件路径（基于 code_output）") String path) {

        Path file = resolveSafePath(path);
        boolean exists = Files.exists(file);

        return VerifyResult.builder()
                .verified(exists)
                .actual(exists ? "EXISTS" : "NOT_EXISTS")
                .build();
    }

    /**
     * 验证文件内容是否与期望一致（精确匹配）
     */
    @Tool(description = "验证文件内容是否与期望内容完全一致")
    public VerifyResult verifyContentEquals(
            @ToolParam(description = "相对文件路径") String path,
            @ToolParam(description = "期望的完整文件内容") String expectedContent) {

        Path file = resolveSafePath(path);

        try {
            String actual = Files.readString(file);
            boolean ok = actual.equals(expectedContent);

            return VerifyResult.builder()
                    .verified(ok)
                    .expected(expectedContent)
                    .actual(actual)
                    .build();

        } catch (Exception e) {
            return VerifyResult.builder()
                    .verified(false)
                    .error(e.getMessage())
                    .build();
        }
    }
}
