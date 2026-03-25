package com.xiaorui.agentapplicationcreator.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    boolean downloadProjectAsZip(String appId, HttpServletResponse response);
}