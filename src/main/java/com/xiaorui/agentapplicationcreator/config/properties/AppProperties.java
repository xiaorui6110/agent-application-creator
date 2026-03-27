package com.xiaorui.agentapplicationcreator.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author xiaorui
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private static final Pattern SAFE_DIRECTORY_NAME = Pattern.compile("^[A-Za-z0-9_-]+$");

    private final Storage storage = new Storage();

    private final Deploy deploy = new Deploy();

    private final Cors cors = new Cors();

    public Path getCodeOutputRootPath() {
        return Path.of(storage.getCodeOutputDir()).toAbsolutePath().normalize();
    }

    public Path getCodeDeployRootPath() {
        return Path.of(storage.getCodeDeployDir()).toAbsolutePath().normalize();
    }

    public Path getCodeVersionRootPath() {
        return Path.of(storage.getCodeVersionDir()).toAbsolutePath().normalize();
    }

    public Path getCodeTemplateRootPath() {
        return Path.of(storage.getCodeTemplateDir()).toAbsolutePath().normalize();
    }

    public Path resolveCodeOutputAppDir(String appId) {
        return resolveChildDir(getCodeOutputRootPath(), appId, "appId");
    }

    public Path resolveCodeDeployAppDir(String deployKey) {
        return resolveChildDir(getCodeDeployRootPath(), deployKey, "deployKey");
    }

    public Path resolveVersionSnapshotDir(String relativePath) {
        return resolvePathWithinRoot(getCodeVersionRootPath(), relativePath);
    }

    public Path resolveTemplateDir(String templateId) {
        return resolveChildDir(getCodeTemplateRootPath(), templateId, "templateId");
    }

    public Path resolvePathWithinRoot(Path rootDir, String relativePath) {
        Path normalizedRoot = rootDir.toAbsolutePath().normalize();
        if (relativePath == null || relativePath.isBlank()) {
            return normalizedRoot;
        }
        Path candidate = Path.of(relativePath);
        if (candidate.isAbsolute()) {
            throw new IllegalArgumentException("absolute path is not allowed: " + relativePath);
        }
        Path resolved = normalizedRoot.resolve(candidate).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("path escapes root directory: " + relativePath);
        }
        return resolved;
    }

    public String buildDeployUrl(String deployKey) {
        validateSafeDirectoryName(deployKey, "deployKey");
        return deploy.getPublicBaseUrl().replaceAll("/+$", "") + "/" + deployKey + "/";
    }

    private Path resolveChildDir(Path rootDir, String childName, String fieldName) {
        return resolvePathWithinRoot(rootDir, validateSafeDirectoryName(childName, fieldName));
    }

    private String validateSafeDirectoryName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is blank");
        }
        if (!SAFE_DIRECTORY_NAME.matcher(value).matches()) {
            throw new IllegalArgumentException(fieldName + " contains illegal characters: " + value);
        }
        return value;
    }

    /**
     * 存储配置
     */
    @Data
    public static class Storage {
        private String codeOutputDir = "./tmp/code_output";
        private String codeDeployDir = "./tmp/code_deploy";
        private String codeVersionDir = "./tmp/code_version";
        private String codeTemplateDir = "./tmp/code_template";
    }

    @Data
    public static class Deploy {
        private String publicBaseUrl = "http://172.19.48.249";
        private String remoteDir = "/home/xiaorui/nginx/code_deploy/";
    }

    @Data
    public static class Cors {
        private final CorsPolicy api = CorsPolicy.defaultApiPolicy();
        private final CorsPolicy statics = CorsPolicy.defaultStaticPolicy();
    }

    @Data
    public static class CorsPolicy {
        private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        private List<String> allowedMethods = new ArrayList<>(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
        private List<String> exposedHeaders = new ArrayList<>(List.of("*"));
        private boolean allowCredentials = true;
        private long maxAge = 3600;

        public static CorsPolicy defaultApiPolicy() {
            return new CorsPolicy();
        }

        public static CorsPolicy defaultStaticPolicy() {
            CorsPolicy policy = new CorsPolicy();
            policy.setAllowedMethods(new ArrayList<>(List.of("GET", "HEAD", "OPTIONS")));
            policy.setAllowCredentials(false);
            return policy;
        }
    }
}