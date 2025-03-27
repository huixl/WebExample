package com.lixh.webexample.service;

import java.util.List;

import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.web.dto.*;

/**
 * 用户服务接口
 */
public interface LoginService {

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    RegisterResponse register(RegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    UserPo findByUsername(String username);

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户
     */
    UserPo getCurrentUser();

    /**
     * 退出登录
     */
    void logout();

    /**
     * 使用指定token退出登录
     *
     * @param token 认证令牌
     */
    void logout(String token);

    /**
     * 刷新用户会话
     *
     * @param token 认证令牌
     * @return 是否刷新成功
     */
    boolean refreshUserSession(String token);

    /**
     * 验证用户当前密码
     *
     * @param passwordVerifyRequest 密码验证请求
     * @return 验证结果，true表示密码正确，false表示密码错误
     */
    boolean verifyCurrentPassword(PasswordVerifyRequest passwordVerifyRequest);

    /**
     * 修改用户密码
     *
     * @param passwordChangeRequest 密码修改请求
     * @return 修改结果
     */
    PasswordChangeResponse changePassword(PasswordChangeRequest passwordChangeRequest);

    /**
     * 刷新用户会话并记录登录历史（如果设备信息变化）
     *
     * @param token      认证令牌
     * @param deviceInfo 设备信息
     * @param loginIp    登录IP
     * @return 是否刷新成功
     */
    boolean refreshUserSessionAndRecordHistory(String token, String deviceInfo, String loginIp);

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    List<UserPo> getAllUsers();
}