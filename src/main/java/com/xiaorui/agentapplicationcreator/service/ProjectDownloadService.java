package com.xiaorui.agentapplicationcreator.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author xiaorui
 */
public interface ProjectDownloadService {

    /**
     * 下载项目为zip文件
     * @param appId 应用ID
     * @param response 响应
     * @return 下载结果
     */
    boolean downloadProjectAsZip(String appId, HttpServletResponse response);
}