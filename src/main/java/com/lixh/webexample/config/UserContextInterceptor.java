package com.lixh.webexample.config;

import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户上下文拦截器
 * 在请求处理前设置用户上下文，在请求处理后清除用户上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final LoginService userService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull Object handler) {
        try {
            // 获取当前登录用户
            UserPo currentUser = userService.getCurrentUser();

            // 设置到ThreadLocal中
            UserContext.setCurrentUser(currentUser);

            if (currentUser != null) {
                log.debug("用户上下文已设置: {}", currentUser.getUsername());
            }
        } catch (Exception e) {
            log.error("设置用户上下文时发生错误", e);
            // 出现异常不影响请求继续处理
        }
        return true;
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull Object handler, ModelAndView modelAndView) {
        // 在这里可以添加其他处理逻辑
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull Object handler, Exception ex) {
        // 清除ThreadLocal，防止内存泄漏
        UserContext.clear();
        log.debug("用户上下文已清除");
    }
}