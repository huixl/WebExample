package com.lixh.login.config;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.lixh.login.annotation.NoRequireLogin;
import com.lixh.login.context.LoginContextHolder;
import com.lixh.login.service.TokenService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录验证拦截器
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //// 如果不是处理方法直接放行
        //if (!(handler instanceof HandlerMethod)) {
        //    return true;
        //}
        //
        //HandlerMethod handlerMethod = (HandlerMethod) handler;
        //
        //// 获取类上的注解
        //NoRequireLogin classAnnotation = handlerMethod.getBeanType().getAnnotation(NoRequireLogin.class);
        //// 获取方法上的注解
        //NoRequireLogin methodAnnotation = handlerMethod.getMethodAnnotation(NoRequireLogin.class);
        //
        //// 如果类和方法上都没有注解，默认需要登录,如果有注解，以方法上的注解为准
        //boolean NoRequireLogin = false;
        //if (methodAnnotation != null) {
        //    NoRequireLogin = methodAnnotation.required();
        //} else if (classAnnotation != null) {
        //    NoRequireLogin = classAnnotation.required();
        //}
        //
        //// 不需要登录直接放行
        //if (NoRequireLogin) {
        //    return true;
        //}
        //
        //// 验证token是否存在
        //String token = LoginContextHolder.getTokenFromCookie(request);
        //if(StringUtils.isEmpty(token)){
        //    return false;
        //}
        //
        //// 验证token是否有效
        //Long userid = tokenService.validateToken(token);
        //if(userid == null){
        //    return false;
        //}

        return true;
    }
} 