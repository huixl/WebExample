package com.lixh.webexample.web.controller;

import com.lixh.webexample.context.LoginContextHolder;
import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.service.UserService;
import com.lixh.webexample.web.dto.LoginInfoResponse;
import com.lixh.webexample.web.dto.LoginRequest;
import com.lixh.webexample.web.dto.LoginResponse;
import com.lixh.webexample.web.dto.RegisterRequest;
import com.lixh.webexample.web.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = userService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
        HttpServletResponse response) {

        LoginResponse loginResponse = userService.login(loginRequest);

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
        HttpServletRequest request,
        HttpServletResponse response) {

        // 从Cookie中获取token并刷新用户会话
        String token = LoginContextHolder.getTokenFromCookie(request);
        if (token != null) {
            // 使用新的方法验证token并记录登录历史（设备信息变化时）
            userService.refreshUserSessionAndRecordHistory(token, request.getHeader("User-Agent"),
                LoginContextHolder.getClientIp(request));
            // 刷新Cookie过期时间
            LoginContextHolder.setAuthCookie(response, token);
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
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        // 从Cookie中获取token
        String token = LoginContextHolder.getTokenFromCookie(request);
        if (token != null) {
            userService.logout(token);
        }

        // 清除认证Cookie
        LoginContextHolder.clearAuthCookie(response);
        return ResponseEntity.ok().build();
    }

}
