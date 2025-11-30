package com.xiaorui.agentapplicationcreator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @description: 密码加密配置
 * @author: xiaorui
 * @date: 2025-11-30 14:34
 **/
@Slf4j
@Configuration
@EnableWebSecurity
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCryptPasswordEncoder initialized");
        // 使用 BCrypt 加密
        return new BCryptPasswordEncoder();
    }

}
