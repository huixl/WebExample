package com.lixh.login.config.permission;

import com.alibaba.fastjson.JSON;
import com.lixh.login.config.UserContext;
import com.lixh.login.data.entity.UserPo;
import com.lixh.login.service.PermissionService;
import com.lixh.login.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 * 拦截需要权限控制的接口请求，检查用户登录状态和权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;

    private final List<String> superUserList = List.of("root");

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler)
            throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 获取方法对象
        Method method = handlerMethod.getMethod();

        // 获取方法上的PermissionCheck注解
        PermissionCheck permissionCheck = method.getAnnotation(PermissionCheck.class);
        if (permissionCheck == null) {
            // 没有权限注解，不需要权限验证，直接放行
            return true;
        }

        // 获取当前用户
        UserPo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未登录，返回未登录错误
            handleUnauthorized(response);
            return false;
        }

        // 超级用户直接放行
        if (superUserList.contains(currentUser.getUsername())) {
            return true;
        }

        // 获取注解上的权限要求
        String[] requiredPermissions = permissionCheck.value();
        if (requiredPermissions.length == 0) {
            // 没有指定权限要求，仅需登录，已通过登录检查，放行
            return true;
        }

        // 权限逻辑（AND或OR）
        Logical logical = permissionCheck.logical();

        // 验证权限
        boolean hasPermission;
        if (logical == Logical.AND) {
            // 需要满足所有权限
            hasPermission = Arrays.stream(requiredPermissions)
                    .allMatch(permission -> permissionService.hasPermission(currentUser.getId(), permission));
        } else {
            // 满足任意一个权限即可
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(permission -> permissionService.hasPermission(currentUser.getId(), permission));
        }

        if (!hasPermission) {
            log.warn("用户 {} 权限不足，尝试访问 {}", currentUser.getUsername(), method.getName());
            handleForbidden(response);
            return false;
        }

        return true;
    }

    /**
     * 处理未授权（未登录）的请求
     */
    private void handleUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        writeErrorResponse(response, HttpStatus.OK.value(), "未登录或会话已过期");
    }

    /**
     * 处理禁止访问（权限不足）的请求
     */
    private void handleForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        writeErrorResponse(response, HttpStatus.OK.value(), "权限不足");
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        final ApiResponse<?> apiResponse = ApiResponse.error(String.valueOf(code), message);

        response.getWriter().write(JSON.toJSONString(apiResponse));
    }
}