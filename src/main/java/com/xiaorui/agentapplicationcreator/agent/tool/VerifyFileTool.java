package com.xiaorui.agentapplicationcreator.agent.tool;

import com.xiaorui.agentapplicationcreator.agent.model.response.VerifyResult;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.file.Files;
import java.nio.file.Path;

// @Component
public class VerifyFileTool {

    private final AppProperties appProperties;

    public VerifyFileTool(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private Path getRootDir() {
        return appProperties.getCodeOutputRootPath();
    }

    private Path resolveSafePath(String relativePath) {
        return appProperties.resolvePathWithinRoot(getRootDir(), relativePath);
    }

    @Tool(description = "Verify whether a file exists under code_output")
    public VerifyResult verifyExists(@ToolParam(description = "Relative file path") String path) {
        Path file = resolveSafePath(path);
        boolean exists = Files.exists(file);
        return VerifyResult.builder()
                .verified(exists)
                .actual(exists ? "EXISTS" : "NOT_EXISTS")
                .build();
    }

    @Tool(description = "Verify whether file content exactly matches expectation")
    public VerifyResult verifyContentEquals(
            @ToolParam(description = "Relative file path") String path,
            @ToolParam(description = "Expected full file content") String expectedContent) {

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