package com.xiaorui.agentapplicationcreator.security.adapter;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 自定义路径
 * @author: xiaorui
 * @date: 2025-11-30 14:42
 **/
@Component
public class ResourceServerAdapter extends DefaultAuthConfigAdapter {

    @Override
    public List<String> pathPatterns() {
        return List.of("/api/**");
    }

    @Override
    public List<String> excludePathPatterns() {
        return List.of(
                "/api/user/register",
                "/api/user/login",
                "/api/user/sendEmailCode",
                "/api/user/getPictureVerifyCode",
                "/api/user/reset/password",
                "/api/app/get/info/**",
                "/api/app/good/list/page/info",
                "/api/static/**",
                "/api/swagger-ui.html",
                "/api/swagger-ui/**",
                "/api/v3/api-docs/**",
                "/api/doc.html",
                "/api/error"
        );
    }
}
