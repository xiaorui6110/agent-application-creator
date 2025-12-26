package com.xiaorui.agentapplicationcreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description: SFTP 配置类，获取 SFTP 连接的 Liunx 服务器信息
 * @author: xiaorui
 * @date: 2025-12-26 14:05
 **/
@Component
@Data
@ConfigurationProperties(prefix = "sftp")
public class SftpConfig {

    private String host;

    private Integer port;

    private String username;

    private String password;

}
