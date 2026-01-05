package com.xiaorui.agentapplicationcreator.constant;

import java.io.File;

/**
 * @description: 应用常量
 * @author: xiaorui
 * @date: 2025-12-22 11:45
 **/
public interface AppConstant {
    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 9;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") +  File.separator + "tmp" + File.separator + "code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_deploy";

    /**
     * 应用部署域名（TODO 改为自己的 Liunx 服务器地址或本地地址）
     */
    String CODE_DEPLOY_HOST = "http://172.30.43.12";

    /**
     * 远程部署目录（TODO 改为自己的 Liunx 服务器目录或本地目录）
     */
    String REMOTE_DEPLOY_DIR = "/home/shenrui/nginx/code_deploy/";

}
