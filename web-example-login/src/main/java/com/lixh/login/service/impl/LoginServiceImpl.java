package com.lixh.login.service.impl;

import java.util.List;
import java.util.Objects;

import com.lixh.login.data.entity.AccountPo;
import com.lixh.login.data.mapper.AccountMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lixh.login.exception.BusinessException;
import com.lixh.login.service.LoginHistoryService;
import com.lixh.login.service.LoginService;
import com.lixh.login.service.TokenService;
import com.lixh.login.strategy.LoginStrategyFactory;
import com.lixh.login.web.dto.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录服务实现类
 * @author lixionghui
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    // 用户会话属性名
    private static final String USER_SESSION_KEY = "current_user";

    // 记住我Cookie名
    private static final String REMEMBER_ME_COOKIE = "remember_token";

    private final AccountMapper userMapper;

    private final TokenService tokenService;

    private final LoginStrategyFactory loginStrategyFactory;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // 1. 验证登录方式不能为空
        if (!StringUtils.hasText(request.getLoginType())) {
            throw new BusinessException("登录方式不能为空");
        }

        // 2. 获取对应的登录策略并处理
        return loginStrategyFactory.getStrategy(request.getLoginType()).processRegister(request);

    }

    @Override
    public LoginResponse login(LoginRequest request) {

        // 1. 验证登录方式不能为空
        if (!StringUtils.hasText(request.getLoginType())) {
            throw new BusinessException("登录方式不能为空");
        }

        // 2. 获取对应的登录策略并处理
        return loginStrategyFactory.getStrategy(request.getLoginType()).processLogin(request);
    }

    @Override
    public AccountPo findByUsername(String username) {
        LambdaQueryWrapper<AccountPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountPo::getNickName, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public AccountPo getCurrentUser() {

        HttpServletRequest request = getRequest();
        HttpSession session = Objects.requireNonNull(request).getSession(false);

        // 从会话中获取用户
        if (session != null) {
            AccountPo user = (AccountPo) session.getAttribute(USER_SESSION_KEY);
            if (user != null) {
                return user;
            }
        }

        // 从Cookie中获取用户
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REMEMBER_ME_COOKIE.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    log.debug("从Cookie中获取token: {}", token);

                    // 验证令牌
                    Long userId = tokenService.validateToken(token);
                    if (userId != null) {
                        // 根据用户ID查询用户
                        AccountPo user = userMapper.selectById(userId);
                        if (user != null) {
                            // 将用户信息保存到会话中
                            request.getSession().setAttribute(USER_SESSION_KEY, user);
                            return user;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void logout(String token) {

        if (token == null || token.isEmpty()) {
            return;
        }

        // 验证令牌
        Long userId = tokenService.validateToken(token);

        if(userId == null){
            return;
        }

        // 删除Redis中的令牌
        tokenService.removeToken(token);

        // 清除当前会话（如果存在）
        HttpServletRequest request = getRequest();
        HttpSession session = Objects.requireNonNull(request).getSession(false);
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
            session.invalidate();
        }

        log.debug("使用token登出成功: {}, 用户ID: {}", token, userId);
    }

    @Override
    public boolean refreshUserSessionAndRecordHistory(String token, String deviceInfo, String loginIp) {

        Long userId = tokenService.validateTokenAndRecordHistory(token, deviceInfo, loginIp);
        if(userId == null){
            return Boolean.FALSE;
        }

        AccountPo user = userMapper.selectById(userId);
        if (user == null) {
            return Boolean.FALSE;
        }

        // 更新会话
        HttpSession session = Objects.requireNonNull(getRequest()).getSession(true);
        session.setAttribute(USER_SESSION_KEY, user);
        return true;

    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.error("获取当前请求失败", e);
            return null;
        }
    }

    /**
     * 获取当前响应
     */
    private HttpServletResponse getResponse() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getResponse() : null;
        } catch (Exception e) {
            log.error("获取当前响应失败", e);
            return null;
        }
    }
}