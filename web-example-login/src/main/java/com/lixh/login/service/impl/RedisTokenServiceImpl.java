package com.lixh.login.service.impl;

import com.lixh.login.service.LoginHistoryService;
import com.lixh.login.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的令牌服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements TokenService {

    // 令牌前缀
    private static final String TOKEN_PREFIX = "user:token:";

    // 用户令牌前缀
    private static final String USER_TOKENS_PREFIX = "user:tokens:";

    // 用户设备信息前缀
    private static final String USER_DEVICE_PREFIX = "user:device:";

    // 默认令牌有效期（秒）- 20分钟
    private static final long DEFAULT_EXPIRE_SECONDS = 20 * 60;

    // 记住我令牌有效期（秒）- 30天
    private static final long REMEMBER_ME_EXPIRE_SECONDS = 30 * 24 * 60 * 60;

    private final RedisTemplate<String, Object> redisTemplate;
    private final LoginHistoryService loginHistoryService;

    @Override
    public String createToken(Long userId, boolean rememberMe) {
        // 生成唯一令牌
        String token = UUID.randomUUID().toString().replace("-", "");

        // 计算过期时间
        long expireSeconds = rememberMe ? REMEMBER_ME_EXPIRE_SECONDS : DEFAULT_EXPIRE_SECONDS;

        // 存储令牌到Redis
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, userId, expireSeconds, TimeUnit.SECONDS);

        // 将令牌添加到用户的令牌集合中
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForSet().add(userTokensKey, token);

        log.debug("创建令牌: {}, 用户ID: {}, 记住我: {}", token, userId, rememberMe);
        return token;
    }

    @Override
    public Long validateToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId != null) {
            log.debug("验证令牌成功: {}, 用户ID: {}", token, userId);
            return Long.valueOf(userId.toString());
        }

        log.debug("验证令牌失败: {}", token);
        return null;
    }

    @Override
    public void removeToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId != null) {
            // 从Redis中删除令牌
            redisTemplate.delete(tokenKey);

            // 从用户的令牌集合中移除令牌
            String userTokensKey = USER_TOKENS_PREFIX + userId;
            redisTemplate.opsForSet().remove(userTokensKey, token);

            log.debug("删除令牌: {}, 用户ID: {}", token, userId);
        }
    }

    @Override
    public void removeUserTokens(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;

        // 获取用户的所有令牌
        var tokens = redisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            // 删除每个令牌
            for (Object token : tokens) {
                String tokenKey = TOKEN_PREFIX + token.toString();
                redisTemplate.delete(tokenKey);
            }

            // 删除用户的令牌集合
            redisTemplate.delete(userTokensKey);

            log.debug("删除用户所有令牌, 用户ID: {}, 令牌数量: {}", userId, tokens.size());
        }
    }

    @Override
    public boolean refreshToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId != null) {
            // 获取当前令牌的剩余过期时间
            Long expireTime = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);

            // 如果令牌即将过期（小于默认过期时间的一半），则刷新过期时间
            if (expireTime > 0) {
                // 判断是否为"记住我"类型的令牌（过期时间大于默认过期时间）
                boolean isRememberMe = expireTime > DEFAULT_EXPIRE_SECONDS;
                long newExpireSeconds = isRememberMe ? REMEMBER_ME_EXPIRE_SECONDS : DEFAULT_EXPIRE_SECONDS;

                // 刷新令牌过期时间
                redisTemplate.expire(tokenKey, newExpireSeconds, TimeUnit.SECONDS);
                log.debug("刷新令牌过期时间: {}, 用户ID: {}, 新过期时间: {}秒", token, userId, newExpireSeconds);
                return true;
            }
        }

        return false;
    }

    @Override
    public long getTokenExpireSeconds() {
        return DEFAULT_EXPIRE_SECONDS;
    }

    @Override
    public Long validateTokenAndRecordHistory(String token, String deviceInfo, String loginIp) {
        // 验证令牌
        Long userId = validateToken(token);

        // 如果令牌有效
        if (userId != null) {
            try {
                // 获取上次登录设备信息
                Map<String, String> lastDeviceInfo = getLastLoginDeviceInfo(userId);

                // 如果设备信息或IP变化，记录新的登录历史
                if (lastDeviceInfo == null ||
                        !Objects.equals(lastDeviceInfo.get("deviceInfo"), deviceInfo) ||
                        !Objects.equals(lastDeviceInfo.get("loginIp"), loginIp)) {

                    // 保存新的设备信息
                    saveLoginDeviceInfo(userId, deviceInfo, loginIp);

                    // 记录登录历史
                    loginHistoryService.recordLoginHistory(
                            userId,
                            loginIp,
                            deviceInfo,
                            1, // 登录成功
                            "TOKEN",
                            "令牌登录成功");
                }
            } catch (Exception e) {
                // 记录异常但不影响主要功能
                log.error("记录令牌登录历史失败", e);
            }

            // 刷新令牌
            refreshToken(token);
        }

        return userId;
    }

    @Override
    public Map<String, String> getLastLoginDeviceInfo(Long userId) {
        String key = USER_DEVICE_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (!entries.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            entries.forEach((k, v) -> result.put(k.toString(), v != null ? v.toString() : null));
            return result;
        }

        return null;
    }

    @Override
    public void saveLoginDeviceInfo(Long userId, String deviceInfo, String loginIp) {
        String key = USER_DEVICE_PREFIX + userId;
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("deviceInfo", deviceInfo);
        deviceMap.put("loginIp", loginIp);

        redisTemplate.opsForHash().putAll(key, deviceMap);
        // 设置与令牌相同的过期时间
        redisTemplate.expire(key, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
}