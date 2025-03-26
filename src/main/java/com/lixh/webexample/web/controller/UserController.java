package com.lixh.webexample.web.controller;

import com.lixh.webexample.config.permission.PermissionCheck;
import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.service.LoginHistoryService;
import com.lixh.webexample.service.UserService;
import com.lixh.webexample.web.dto.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    // Cookie名称常量
    private static final String AUTH_COOKIE_NAME = "orderiq_auth_token";

    // Cookie过期时间（20分钟）
    private static final int COOKIE_MAX_AGE = 20 * 60;

    // Cookie路径
    private static final String COOKIE_PATH = "/";

    // 是否仅HTTPS
    private static final boolean COOKIE_SECURE = false; // 生产环境应设为true

    // 是否禁止JavaScript访问
    private static final boolean COOKIE_HTTP_ONLY = true;

    // Cookie的SameSite属性
    private static final String COOKIE_SAME_SITE = "Lax";

    private final UserService userService;
    private final LoginHistoryService loginHistoryService;

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = userService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @param response     HTTP响应对象，用于设置Cookie
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        LoginResponse loginResponse = userService.login(loginRequest);

        // 如果登录成功，设置认证Cookie
        if (loginResponse != null && loginResponse.getToken() != null) {
            setAuthCookie(response, loginResponse.getToken());
        }

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request  HTTP请求对象，用于获取Cookie
     * @param response HTTP响应对象，用于刷新Cookie
     * @return 登录信息
     */
    @GetMapping("/loginInfo")
    public ResponseEntity<LoginInfoResponse> getLoginInfo(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 从Cookie中获取token并刷新用户会话
        String token = getTokenFromCookie(request);
        if (token != null) {
            // 使用新的方法验证token并记录登录历史（设备信息变化时）
            userService.refreshUserSessionAndRecordHistory(token, request.getHeader("User-Agent"),
                    getClientIp(request));
            // 刷新Cookie过期时间
            setAuthCookie(response, token);
        }

        UserPo user = userService.getCurrentUser();

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        LoginInfoResponse loginInfoResponse = LoginInfoResponse.builder()
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build();

        return ResponseEntity.ok(loginInfoResponse);
    }

    /**
     * 退出登录
     *
     * @param request  HTTP请求对象，用于获取Cookie
     * @param response HTTP响应对象，用于清除Cookie
     * @return 成功响应
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 从Cookie中获取token
        String token = getTokenFromCookie(request);
        if (token != null) {
            userService.logout(token);
        }

        // 清除认证Cookie
        clearAuthCookie(response);

        return ResponseEntity.ok().build();
    }

    /**
     * 设置认证Cookie
     *
     * @param response HTTP响应对象
     * @param token    认证令牌
     */
    private void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .maxAge(COOKIE_MAX_AGE)
                .path(COOKIE_PATH)
                .secure(COOKIE_SECURE)
                .httpOnly(COOKIE_HTTP_ONLY)
                .sameSite(COOKIE_SAME_SITE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 清除认证Cookie
     *
     * @param response HTTP响应对象
     */
    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .maxAge(0)
                .path(COOKIE_PATH)
                .secure(COOKIE_SECURE)
                .httpOnly(COOKIE_HTTP_ONLY)
                .sameSite(COOKIE_SAME_SITE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 从Cookie中获取认证令牌
     *
     * @param request HTTP请求对象
     * @return 认证令牌，如果不存在则返回null
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 验证当前密码
     *
     * @param passwordVerifyRequest 密码验证请求
     * @return 验证结果
     */
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@Valid @RequestBody PasswordVerifyRequest passwordVerifyRequest) {
        boolean result = userService.verifyCurrentPassword(passwordVerifyRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改密码
     *
     * @param passwordChangeRequest 密码修改请求
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest,
            HttpServletResponse response) {
        PasswordChangeResponse result = userService.changePassword(passwordChangeRequest);

        // 如果密码修改成功，清除认证Cookie
        if (result.isSuccess()) {
            clearAuthCookie(response);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户登录历史
     *
     * @param limit 限制数量，默认为10
     * @return 登录历史列表
     */
    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistoryResponse>> getLoginHistory(@RequestParam(required = false) Integer limit) {
        UserPo currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<LoginHistoryResponse> history = loginHistoryService.getUserLoginHistory(currentUser.getId(), limit);
        return ResponseEntity.ok(history);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
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

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    @PermissionCheck("USER:VIEW")
    public ResponseEntity<List<UserPo>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}