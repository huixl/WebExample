package com.lixh.webexample.web.controller;

import com.lixh.webexample.config.permission.PermissionCheck;
import com.lixh.webexample.constant.CookieConstant;
import com.lixh.webexample.context.LoginContextHolder;
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

    private final UserService userService;

    private final LoginHistoryService loginHistoryService;

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
            LoginContextHolder.clearAuthCookie(response);
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