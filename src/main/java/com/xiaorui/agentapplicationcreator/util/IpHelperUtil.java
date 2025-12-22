package com.xiaorui.agentapplicationcreator.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @description: IP 工具类
 * @author: xiaorui
 * @date: 2025-11-30 14:26
 **/
public class IpHelperUtil {

    /**
     * 未知IP
     */
    private static final String UNKNOWN = "unknown";

    /**
     * 得到用户的真实地址,如果有多个就取第一个
     */
    public static String getIpAddr() {
        HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String[] ips = ip.split(",");
        return ips[0].trim();
    }

}
