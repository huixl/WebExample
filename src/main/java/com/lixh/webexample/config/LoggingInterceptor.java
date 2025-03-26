package com.lixh.webexample.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request Method: {}", request.getMethod());
        log.info("Request Headers: {}", request.getHeaderNames());
        log.info("Request Parameters: {}", request.getParameterMap());
        return true;
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest request, HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) {
        log.info("Response Status: {}", response.getStatus());
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        if (ex != null) {
            log.error("Exception occurred: ", ex);
        }
    }
}