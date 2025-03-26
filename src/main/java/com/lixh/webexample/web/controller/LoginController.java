package com.lixh.webexample.web.controller;

import com.lixh.webexample.context.LoginContextHolder;
import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.service.LoginHistoryService;
import com.lixh.webexample.service.LoginService;
import com.lixh.webexample.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    private final LoginHistoryService loginHistoryService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = loginService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
        HttpServletResponse response) {

        LoginResponse loginResponse = loginService.login(loginRequest);

        // 如果登录成功，设置认证Cookie
        if (loginResponse != null && loginResponse.getToken() != null) {
            LoginContextHolder.setAuthCookie(response, loginResponse.getToken());
        }

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/loginInfo")
    public ResponseEntity<LoginInfoResponse> getLoginInfo(
        HttpServletRequest request, HttpServletResponse response) {

        // 从Cookie中获取token并刷新用户会话
        String token = LoginContextHolder.getTokenFromCookie(request);
        if (token != null) {
            // 使用新的方法验证token并记录登录历史（设备信息变化时）
            loginService.refreshUserSessionAndRecordHistory(token, request.getHeader("User-Agent"),
                LoginContextHolder.getClientIp(request));
            // 刷新Cookie过期时间
            LoginContextHolder.setAuthCookie(response, token);
        }

        UserPo user = loginService.getCurrentUser();

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
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        // 从Cookie中获取token
        String token = LoginContextHolder.getTokenFromCookie(request);
        if (token != null) {
            loginService.logout(token);
        }

        // 清除认证Cookie
        LoginContextHolder.clearAuthCookie(response);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户登录历史
     *
     * @param limit 限制数量，默认为10
     * @return 登录历史列表
     */
    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistoryResponse>> getLoginHistory(@RequestParam(required = false) Integer limit) {
        UserPo currentUser = loginService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<LoginHistoryResponse> history = loginHistoryService.getUserLoginHistory(currentUser.getId(), limit);
        return ResponseEntity.ok(history);
    }

    /**
     * 验证当前密码
     *
     * @param passwordVerifyRequest 密码验证请求
     * @return 验证结果
     */
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@Valid @RequestBody PasswordVerifyRequest passwordVerifyRequest) {
        boolean result = loginService.verifyCurrentPassword(passwordVerifyRequest);
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
        PasswordChangeResponse result = loginService.changePassword(passwordChangeRequest);

        // 如果密码修改成功，清除认证Cookie
        if (result.isSuccess()) {
            LoginContextHolder.clearAuthCookie(response);
        }

        return ResponseEntity.ok(result);
    }

}
