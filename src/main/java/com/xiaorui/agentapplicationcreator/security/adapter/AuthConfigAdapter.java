package com.xiaorui.agentapplicationcreator.security.adapter;

import java.util.List;

/**
 * @description: 实现该接口之后，修改需要授权登陆的路径，不需要授权登陆的路径
 * @author: xiaorui
 * @date: 2025-11-30 14:41
 **/
public interface AuthConfigAdapter {
    /**
     * 也许需要登录才可用的url
     */
    String MAYBE_AUTH_URI = "/**/api/**";

    /**
     * 需要授权登陆的路径
     * @return 需要授权登陆的路径列表
     */
    List<String> pathPatterns();

    /**
     * 不需要授权登陆的路径
     * @return 不需要授权登陆的路径列表
     */
    List<String> excludePathPatterns();

}
