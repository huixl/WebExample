package com.lixh.login.web.controller;

import com.lixh.login.context.LoginContextHolder;
import com.lixh.login.data.entity.AccountPo;
import com.lixh.login.service.LoginHistoryService;
import com.lixh.login.service.LoginService;
import com.lixh.login.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    private final LoginHistoryService loginHistoryService;

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
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = loginService.register(registerRequest);
        return ResponseEntity.ok(response);
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
            loginService.refreshUserSessionAndRecordHistory(token, request.getHeader("User-Agent"),
                LoginContextHolder.getClientIp(request));
            // 刷新Cookie过期时间
            LoginContextHolder.setAuthCookie(response, token);
        }

        AccountPo user = loginService.getCurrentUser();

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        LoginInfoResponse loginInfoResponse = LoginInfoResponse.builder()
            .username(user.getNickName())
            .avatar(user.getAvatarUrl())
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
     * 测试
     * @return
     */
    @GetMapping("/test")
    public ResponseEntity<Boolean> test() {
        System.out.println("-----");
        System.out.println("join");
        System.out.println("-----");
        return ResponseEntity.ok(Boolean.TRUE);
    }

}
