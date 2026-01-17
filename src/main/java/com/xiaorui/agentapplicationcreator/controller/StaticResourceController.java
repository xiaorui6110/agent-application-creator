package com.xiaorui.agentapplicationcreator.controller;

import com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerMapping;

import java.io.File;

import static com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum.DEPLOY;
import static com.xiaorui.agentapplicationcreator.enums.StaticVisitTypeEnum.PREVIEW;

/**
 * @description: 静态资源访问控制器
 * @author: xiaorui
 * @date: 2025-12-25 15:33
 **/
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    /**
     * 访问预览应用
     * 提供静态资源访问，支持目录重定向
     * 访问格式：<a href="http://localhost:8180/api/static/preview/fileName">...</a>
     */
    @GetMapping("/preview/{fileName}/**")
    public ResponseEntity<Resource> serveStaticPreviewResource(@PathVariable String fileName, HttpServletRequest request) {
        return getResource(StaticVisitTypeEnum.PREVIEW, fileName, request);
    }

    /**
     * 访问部署好的应用
     * 提供静态资源访问，支持目录重定向
     * 访问格式：<a href="http://localhost:8180/api/static/deploy/deployKey">...</a>
     */
    @GetMapping("/deploy/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticDeployResource(@PathVariable String deployKey, HttpServletRequest request) {
        return getResource(StaticVisitTypeEnum.DEPLOY, deployKey, request);
    }

    /**
     * 提供静态资源访问 - (预览/部署)
     *
     * @param staticVisitTypeEnum 预览/部署 枚举值
     * @param fileNameOrDeployKey 预览文件名或部署ID
     * @param request ServletRequest
     * @return 请求的资源
     */
    private ResponseEntity<Resource> getResource(StaticVisitTypeEnum staticVisitTypeEnum, String fileNameOrDeployKey, HttpServletRequest request) {
        // 获取资源路径
        String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        // 预览
        if (PREVIEW.equals(staticVisitTypeEnum)) {
            resourcePath = resourcePath.substring(("/static/preview/" + fileNameOrDeployKey).length());
        } else if (DEPLOY.equals(staticVisitTypeEnum)) {
            resourcePath = resourcePath.substring(("/static/deploy/" + fileNameOrDeployKey).length());
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知访问类型");
        }

        try {
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回 index.html
            if ("/".equals(resourcePath)) {
                resourcePath = "/index.html";
            }
            // 构建文件路径
            String filePath  = staticVisitTypeEnum.getValue() + File.separator + fileNameOrDeployKey + resourcePath;
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (filePath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (filePath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (filePath.endsWith(".png")) {
            return "image/png";
        }
        if (filePath.endsWith(".jpg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }



}
