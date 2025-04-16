package com.lixh.login.constant;

/**
 * @Description
 * @Author xionghui.lxh
 * @Date 2025-03-26 17:24
 */


public class CookieConstant {

    /**
     * Cookie名称常量
     */
    public static final String AUTH_COOKIE_NAME = "web_auth_token";

    /**
     * Cookie过期时间（20分钟）
     */
    public static final int COOKIE_MAX_AGE = 20 * 60;

    /**
     * Cookie路径
     */
    public static final String COOKIE_PATH = "/";

    /**
     * 是否仅HTTPS
     */
    public static final boolean COOKIE_SECURE = false;

    /**
     * 是否禁止JavaScript访问
     */
    public static final boolean COOKIE_HTTP_ONLY = true;

    /**
     * Cookie的SameSite属性
     * Strict（严格）, Lax（宽松）, None（无） 默认Strict
     */
    public static final String COOKIE_SAME_SITE = "Lax";

}
