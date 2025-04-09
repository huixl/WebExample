package com.lixh.login.service;

import java.util.Map;

/**
 * 令牌服务接口
 */
public interface TokenService {

    /**
     * 创建令牌
     *
     * @param userId     用户ID
     * @param rememberMe 是否记住我
     * @return 令牌
     */
    String createToken(Long userId, boolean rememberMe);

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 用户ID，如果令牌无效则返回null
     */
    Long validateToken(String token);

    /**
     * 验证令牌并记录登录历史（如果需要）
     *
     * @param token      令牌
     * @param deviceInfo 设备信息
     * @param loginIp    登录IP
     * @return 用户ID，如果令牌无效则返回null
     */
    Long validateTokenAndRecordHistory(String token, String deviceInfo, String loginIp);

    /**
     * 删除令牌
     *
     * @param token 令牌
     */
    void removeToken(String token);

    /**
     * 删除用户的所有令牌
     *
     * @param userId 用户ID
     */
    void removeUserTokens(Long userId);

    /**
     * 刷新令牌有效期
     *
     * @param token 令牌
     * @return 是否刷新成功
     */
    boolean refreshToken(String token);

    /**
     * 获取令牌过期时间（秒）
     *
     * @return 令牌过期时间
     */
    long getTokenExpireSeconds();

    /**
     * 获取用户最后一次登录的设备信息
     *
     * @param userId 用户ID
     * @return 设备信息映射，包含deviceInfo和loginIp，如果不存在则返回null
     */
    Map<String, String> getLastLoginDeviceInfo(Long userId);

    /**
     * 保存用户登录设备信息
     *
     * @param userId     用户ID
     * @param deviceInfo 设备信息
     * @param loginIp    登录IP
     */
    void saveLoginDeviceInfo(Long userId, String deviceInfo, String loginIp);
}