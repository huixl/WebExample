package com.lixh.webexample.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 */
@Component
public class DistributedLockUtil {

    // 释放锁的Lua脚本
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    // 锁前缀
    private static final String LOCK_PREFIX = "distributed_lock:";

    // 默认锁过期时间（秒）
    private static final long DEFAULT_LOCK_TIMEOUT = 30;

    private final RedisTemplate<String, Object> redisTemplate;

    public DistributedLockUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁的键
     * @param timeout 锁的过期时间（秒）
     * @return 锁的值（用于解锁），如果获取失败则返回null
     */
    public String tryLock(String lockKey, long timeout) {
        String fullLockKey = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                fullLockKey, lockValue, timeout, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success) ? lockValue : null;
    }

    /**
     * 尝试获取分布式锁（使用默认过期时间）
     *
     * @param lockKey 锁的键
     * @return 锁的值（用于解锁），如果获取失败则返回null
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_LOCK_TIMEOUT);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁的键
     * @param lockValue 锁的值（获取锁时返回的值）
     * @return 是否成功释放锁
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        String fullLockKey = LOCK_PREFIX + lockKey;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RELEASE_LOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(
                redisScript, Collections.singletonList(fullLockKey), lockValue);

        return result != null && result == 1;
    }
} 