package com.lixh.webexample.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.data.mapper.UserMapper;
import com.lixh.webexample.exception.BusinessException;
import com.lixh.webexample.service.LoginHistoryService;
import com.lixh.webexample.service.LoginService;
import com.lixh.webexample.service.TokenService;
import com.lixh.webexample.strategy.LoginStrategyFactory;
import com.lixh.webexample.web.dto.*;

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

    // 记住我Cookie有效期（秒）- 30天
    private static final int REMEMBER_ME_COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final LoginHistoryService loginHistoryService;

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
    public UserPo findByUsername(String username) {
        LambdaQueryWrapper<UserPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPo::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public UserPo getCurrentUser() {
        HttpServletRequest request = getRequest();
        HttpSession session = Objects.requireNonNull(request).getSession(false);

        // 从会话中获取用户
        if (session != null) {
            UserPo user = (UserPo) session.getAttribute(USER_SESSION_KEY);
            if (user != null) {
                return user;
            }
        }

        // 尝试从记住我Cookie中恢复用户
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
                        UserPo user = userMapper.selectById(userId);
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
    public void logout() {

    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 验证令牌
            Long userId = tokenService.validateToken(token);

            if (userId != null) {
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
        }
    }

    @Override
    public boolean refreshUserSession(String token) {
        if (token != null && !token.isEmpty()) {
            // 验证并刷新令牌
            Long userId = tokenService.validateToken(token);

            if (userId != null) {
                // 刷新令牌有效期
                boolean refreshed = tokenService.refreshToken(token);

                // 如果当前会话中没有用户信息，则加载用户信息到会话
                HttpServletRequest request = getRequest();
                HttpSession session = Objects.requireNonNull(request).getSession(false);

                if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
                    // 根据用户ID查询用户
                    UserPo user = userMapper.selectById(userId);
                    if (user != null) {
                        // 将用户信息保存到会话中
                        request.getSession().setAttribute(USER_SESSION_KEY, user);
                    }
                }

                log.debug("刷新用户会话成功: {}, 用户ID: {}", token, userId);
                return refreshed;
            }
        }

        return false;
    }

    @Override
    public boolean verifyCurrentPassword(PasswordVerifyRequest passwordVerifyRequest) {
        UserPo currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        boolean matches = passwordEncoder.matches(passwordVerifyRequest.getCurrentPassword(),
                currentUser.getPassword());

        // 记录密码验证历史（使用try-catch包裹，确保即使记录历史失败也不影响验证结果）
        try {
            loginHistoryService.recordLoginHistory(
                    currentUser.getId(),
                    getClientIp(),
                    Objects.requireNonNull(getRequest()).getHeader("User-Agent"),
                    matches ? 1 : 0,
                    "PASSWORD_VERIFY",
                    "密码验证" + (matches ? "成功" : "失败"));
        } catch (Exception e) {
            // 记录异常但不抛出，不影响主要功能
            log.error("记录密码验证历史失败", e);
        }

        return matches;
    }

    @Override
    @Transactional
    public PasswordChangeResponse changePassword(PasswordChangeRequest passwordChangeRequest) {
        // 1. 验证用户是否登录
        UserPo currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 2. 验证当前密码是否正确
        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), currentUser.getPassword())) {
            return PasswordChangeResponse.builder()
                    .success(false)
                    .message("当前密码不正确")
                    .build();
        }

        // 3. 验证新密码与确认密码是否一致
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            return PasswordChangeResponse.builder()
                    .success(false)
                    .message("新密码与确认密码不一致")
                    .build();
        }

        // 4. 验证新密码是否与当前密码相同
        if (passwordEncoder.matches(passwordChangeRequest.getNewPassword(), currentUser.getPassword())) {
            return PasswordChangeResponse.builder()
                    .success(false)
                    .message("新密码不能与当前密码相同")
                    .build();
        }

        // 5. 更新密码
        String newEncodedPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());
        currentUser.setPassword(newEncodedPassword);
        userMapper.updateById(currentUser);

        // 6. 记录密码修改历史
        try {
            loginHistoryService.recordLoginHistory(
                    currentUser.getId(),
                    getClientIp(),
                    Objects.requireNonNull(getRequest()).getHeader("User-Agent"),
                    1,
                    "PASSWORD_CHANGE",
                    "密码修改成功");
        } catch (Exception e) {
            // 记录异常但不抛出，不影响主要功能
            log.error("记录密码修改历史失败", e);
        }

        // 7. 清除所有会话，强制重新登录
        tokenService.removeUserTokens(currentUser.getId());
        logout();

        return PasswordChangeResponse.builder()
                .success(true)
                .message("密码修改成功，请重新登录")
                .build();
    }

    @Override
    public boolean refreshUserSessionAndRecordHistory(String token, String deviceInfo, String loginIp) {
        // 验证令牌并记录登录历史（如果需要）
        Long userId = tokenService.validateTokenAndRecordHistory(token, deviceInfo, loginIp);

        if (userId != null) {
            // 查询用户信息
            UserPo user = userMapper.selectById(userId);
            if (user != null) {
                // 更新会话
                HttpSession session = Objects.requireNonNull(getRequest()).getSession(true);
                session.setAttribute(USER_SESSION_KEY, user);
                return true;
            }
        }

        return false;
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

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return "unknown";
            }

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // 对于通过多个代理的情况，第一个IP为客户端真实IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            return ip != null ? ip : "unknown";
        } catch (Exception e) {
            log.error("获取客户端IP失败", e);
            return "unknown";
        }
    }

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    @Override
    public List<UserPo> getAllUsers() {
        LambdaQueryWrapper<UserPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPo::getStatus, 1)
                .orderByDesc(UserPo::getCreateTime);
        return userMapper.selectList(queryWrapper);
    }
}