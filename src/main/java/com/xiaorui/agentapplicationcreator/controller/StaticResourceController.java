package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.util.StrUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

import static com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum.DEPLOY;
import static com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum.PREVIEW;

@RestController
@RequestMapping("/static")
public class StaticResourceController {

    @Resource
    private AppService appService;

    @GetMapping("/preview/{appId}/**")
    @Operation(summary = "访问预览应用", description = "访问预览应用")
    @Parameter(name = "appId", description = "应用ID")
    public ResponseEntity<Resource> serveStaticPreviewResource(@PathVariable String appId, HttpServletRequest request) {
        return getResource(StaticVisitTypeEnum.PREVIEW, appId, request);
    }

    @GetMapping("/deploy/{deployKey}/**")
    @Operation(summary = "访问部署应用", description = "访问部署应用")
    @Parameter(name = "deployKey", description = "部署key")
    public ResponseEntity<Resource> serveStaticDeployResource(@PathVariable String deployKey, HttpServletRequest request) {
        return getResource(StaticVisitTypeEnum.DEPLOY, deployKey, request);
    }

    private ResponseEntity<Resource> getResource(StaticVisitTypeEnum staticVisitTypeEnum, String appIdOrDeployKey,
                                                 HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String prefix;
        if (PREVIEW.equals(staticVisitTypeEnum)) {
            prefix = "/api/static/preview/" + appIdOrDeployKey;
        } else if (DEPLOY.equals(staticVisitTypeEnum)) {
            prefix = "/api/static/deploy/" + appIdOrDeployKey;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知访问类型");
        }

        String resourcePath = requestUri.substring(prefix.length());
        if (resourcePath.contains("..")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            if ("/".equals(resourcePath)) {
                String defaultEntry = resolveDefaultEntry(staticVisitTypeEnum, appIdOrDeployKey);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + defaultEntry.substring(1));
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }

            String projectPath = System.getProperty("user.dir");
            String filePath = projectPath + File.separator +
                    staticVisitTypeEnum.getValue() + File.separator + appIdOrDeployKey + resourcePath;
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }
            FileSystemResource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getContentTypeWithCharset(filePath))
                    .body((Resource) resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
