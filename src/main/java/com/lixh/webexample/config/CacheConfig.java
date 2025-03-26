package com.lixh.webexample.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置自定义的Redis Jackson序列化器
     * 支持Java 8日期时间类型（如LocalDateTime）
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会抛出异常
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 添加Java 8日期时间模块支持
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳的功能，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    /**
     * 配置Redis缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 设置默认过期时间：30分钟
                .entryTtl(Duration.ofMinutes(30))
                // 设置key的序列化器
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value的序列化器 - 使用自定义的Jackson序列化器
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer()))
                // 不缓存null值
                .disableCachingNullValues();

        // 特定缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 权限检查缓存 - 1小时过期
        cacheConfigurations.put("permissionCheck", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        // 权限列表缓存 - 2小时过期
        cacheConfigurations.put("permissions", defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // 权限代码缓存 - 2小时过期
        cacheConfigurations.put("permissionCodes", defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // 角色列表缓存 - 2小时过期
        cacheConfigurations.put("roles", defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // 角色权限缓存 - 2小时过期
        cacheConfigurations.put("rolePermissions", defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // 用户角色缓存 - 1小时过期
        cacheConfigurations.put("userRoles", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        // 原有缓存
        cacheConfigurations.put("templates", defaultCacheConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("api_responses", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        // 创建Redis缓存管理器
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}