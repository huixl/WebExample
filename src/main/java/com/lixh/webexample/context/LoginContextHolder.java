package com.lixh.webexample.context;

import com.lixh.webexample.constant.CookieConstant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * @Description
 * @Author xionghui.lxh
 * @Date 2025-03-26 17:32
 */
public class LoginContextHolder {

    /**
     * 设置认证Cookie
     * @param response HTTP响应对象
     * @param token    认证令牌
     */
    public static void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstant.AUTH_COOKIE_NAME, token)
            .maxAge(CookieConstant.COOKIE_MAX_AGE)
            .path(CookieConstant.COOKIE_PATH)
            .secure(CookieConstant.COOKIE_SECURE)
            .httpOnly(CookieConstant.COOKIE_HTTP_ONLY)
            .sameSite(CookieConstant.COOKIE_SAME_SITE)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 清除认证Cookie
     * @param response HTTP响应对象
     */
    public static  void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstant.AUTH_COOKIE_NAME, "")
            .maxAge(0)
            .path(CookieConstant.COOKIE_PATH)
            .secure(CookieConstant.COOKIE_SECURE)
            .httpOnly(CookieConstant.COOKIE_HTTP_ONLY)
            .sameSite(CookieConstant.COOKIE_SAME_SITE)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 从Cookie中获取认证令牌
     * @param request HTTP请求对象
     * @return 认证令牌，如果不存在则返回null
     */
    public static  String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CookieConstant.AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static  String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
            // XFF格式为: client, proxy1, proxy2, ... 取第一个非unknown的IP
            String[] ips = xff.split(",");
            for (String ip : ips) {
                ip = ip.trim();
                if (!"unknown".equalsIgnoreCase(ip)) {
                    return ip;
                }
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

}
