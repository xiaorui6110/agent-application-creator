package com.xiaorui.agentapplicationcreator.security.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.manager.token.TokenStoreManager;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoInTokenBO;
import com.xiaorui.agentapplicationcreator.response.ResponseEnum;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.security.adapter.AuthConfigAdapter;
import com.xiaorui.agentapplicationcreator.security.handler.HttpHandler;
import com.xiaorui.agentapplicationcreator.util.AuthUserContextUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

/**
 * @description: 授权过滤，只要实现AuthConfigAdapter接口，添加对应路径即可
 * @author: xiaorui
 * @date: 2025-11-30 14:41
 **/
@Component
public class AuthFilter implements Filter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Resource
    private AuthConfigAdapter authConfigAdapter;

    @Resource
    private HttpHandler httpHandler;

    @Resource
    private TokenStoreManager tokenStoreManager;

    @Value("${sa-token.token-name:xiaorui}")
    private String tokenName;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, resp);
            return;
        }

        String requestURL = req.getRequestURI();
        boolean excluded = matches(authConfigAdapter.excludePathPatterns(), requestURL);
        boolean needAuth = matches(authConfigAdapter.pathPatterns(), requestURL) && !excluded;
        String accessToken = resolveAccessToken(req);
        UserInfoInTokenBO userInfoInToken = null;

        try {
            if (needAuth) {
                userInfoInToken = authenticate(accessToken);
                if (userInfoInToken == null) {
                    httpHandler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
                    return;
                }
            } else if (StrUtil.isNotBlank(accessToken)) {
                userInfoInToken = authenticate(accessToken);
            }

            AuthUserContextUtil.set(userInfoInToken);
            chain.doFilter(req, resp);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                httpHandler.printServerResponseToWeb((BusinessException) e);
            } else {
                throw e;
            }
        } finally {
            AuthUserContextUtil.clean();
        }
    }

    private boolean matches(List<String> pathPatterns, String requestURL) {
        if (CollectionUtil.isEmpty(pathPatterns)) {
            return false;
        }
        for (String pathPattern : pathPatterns) {
            if (pathMatcher.match(pathPattern, requestURL)) {
                return true;
            }
        }
        return false;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String accessToken = request.getHeader(tokenName);
        if (StrUtil.isNotBlank(accessToken)) {
            return accessToken;
        }
        return StpUtil.getTokenValue();
    }

    private UserInfoInTokenBO authenticate(String accessToken) {
        if (StrUtil.isBlank(accessToken)) {
            return null;
        }
        try {
            StpUtil.getLoginIdByToken(accessToken);
            return tokenStoreManager.getUserInfoByAccessToken(accessToken, true);
        } catch (Exception e) {
            return null;
        }
    }
}
