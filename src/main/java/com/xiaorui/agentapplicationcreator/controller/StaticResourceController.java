package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.nio.file.Path;

import static com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum.DEPLOY;
import static com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum.PREVIEW;

/**
 * @author xiaorui
 */
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    @Resource
    private AppService appService;

    @Resource
    private AppProperties appProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @GetMapping("/preview/{appId}/**")
    @Operation(summary = "访问预览应用", description = "访问预览应用")
    @Parameter(name = "appId", description = "应用 ID")
    public ResponseEntity<org.springframework.core.io.Resource> serveStaticPreviewResource(@PathVariable String appId, HttpServletRequest request) {
        return getResource(PREVIEW, appId, request);
    }

    @GetMapping("/deploy/{deployKey}/**")
    @Operation(summary = "访问部署应用", description = "访问部署应用")
    @Parameter(name = "deployKey", description = "部署 key")
    public ResponseEntity<org.springframework.core.io.Resource> serveStaticDeployResource(@PathVariable String deployKey, HttpServletRequest request) {
        return getResource(DEPLOY, deployKey, request);
    }

    private ResponseEntity<org.springframework.core.io.Resource> getResource(StaticVisitTypeEnum visitType, String appIdOrDeployKey,
                                                 HttpServletRequest request) {
        try {
            String resourcePath = resolveResourcePath(request);
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            if ("/".equals(resourcePath)) {
                String defaultEntry = resolveDefaultEntry(visitType, appIdOrDeployKey);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + defaultEntry.substring(1));
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }

            Path basePath = resolveBasePath(visitType, appIdOrDeployKey);
            Path resolvedPath = appProperties.resolvePathWithinRoot(basePath, resourcePath.substring(1));

            File file = resolvedPath.toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getContentTypeWithCharset(file.getName()))
                    .body(new FileSystemResource(file));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String resolveResourcePath(HttpServletRequest request) {
        String pathWithinMapping = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String resourcePath = "/" + antPathMatcher.extractPathWithinPattern(bestMatchingPattern, pathWithinMapping);
        if (resourcePath.contains("..")) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "no access");
        }
        return resourcePath;
    }

    private Path resolveBasePath(StaticVisitTypeEnum visitType, String appIdOrDeployKey) {
        if (PREVIEW.equals(visitType)) {
            return appProperties.resolveCodeOutputAppDir(appIdOrDeployKey);
        }
        if (DEPLOY.equals(visitType)) {
            return appProperties.resolveCodeDeployAppDir(appIdOrDeployKey);
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "the visit type is not supported");
    }

    private String resolveDefaultEntry(StaticVisitTypeEnum visitType, String appIdOrDeployKey) {
        if (DEPLOY.equals(visitType)) {
            return "/index.html";
        }
        App app = appService.getById(appIdOrDeployKey);
        if (app != null && CodeGenTypeEnum.VUE_PROJECT.getValue().equals(app.getCodeGenType())) {
            return "/dist/index.html";
        }
        return "/index.html";
    }

    private String getContentTypeWithCharset(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (lowerPath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (lowerPath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        }
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lowerPath.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (lowerPath.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (StrUtil.endWithAnyIgnoreCase(lowerPath, ".woff", ".woff2")) {
            return "font/woff2";
        }
        return "application/octet-stream";
    }
}
