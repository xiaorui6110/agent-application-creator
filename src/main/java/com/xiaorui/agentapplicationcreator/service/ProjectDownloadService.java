package com.xiaorui.agentapplicationcreator.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @description: 项目下载服务接口
 * @author: xiaorui
 * @date: 2026-01-02 15:55
 **/
public interface ProjectDownloadService {

    /**
     * 下载项目为压缩包
     *
     * @param projectPath 项目路径
     * @param downloadFileName 下载文件名称
     * @param response HttpServletResponse对象
     * @return 下载成功返回true，失败返回false
     */
    boolean downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);

}
