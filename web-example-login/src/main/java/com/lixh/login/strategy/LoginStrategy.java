package com.lixh.login.strategy;

import com.lixh.login.web.dto.LoginRequest;
import com.lixh.login.web.dto.LoginResponse;
import com.lixh.login.web.dto.RegisterRequest;
import com.lixh.login.web.dto.RegisterResponse;

/**
 * 登录策略接口
 * @author lixionghui
 */
public interface LoginStrategy {
    
    /**
     * 获取登录方式
     * @return 登录方式代码
     */
    String getLoginType();
    
    /**
     * 处理登录请求
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse processLogin(LoginRequest request);

    /**
     * 处理注册请求
     * @param request 登录请求
     * @return 登录响应
     */
    RegisterResponse processRegister(RegisterRequest request);

    /**
     * 处理退出登录请求
     * @return 登录响应
     */
    void processLogout();
} 