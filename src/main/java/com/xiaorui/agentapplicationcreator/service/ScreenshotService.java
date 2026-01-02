package com.xiaorui.agentapplicationcreator.service;

/**
 * @description: 截图服务接口
 * @author: xiaorui
 * @date: 2026-01-02 16:02
 **/
public interface ScreenshotService {

    /**
     * 通用的截图服务，可以得到访问地址
     *
     * @param webUrl 网址
     * @return 截图访问地址
     */
    String createAndUploadScreenshot(String webUrl);

}
