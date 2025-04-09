package com.lixh.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web安全配置
 * 不使用Spring Security进行权限控制，全部使用自定义权限系统
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * 安全过滤器链配置
     * 所有请求全部放行，权限控制由自定义系统实现
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护
                .csrf(AbstractHttpConfigurer::disable)
                // 配置请求授权
                .authorizeHttpRequests(authorize -> authorize
                        // 所有请求全部放行，权限控制由自定义系统实现
                        .anyRequest().permitAll())
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}