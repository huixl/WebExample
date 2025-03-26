package com.lixh.webexample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 管理员配置
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "orderiq.admin")
public class AdminConfig {

    /**
     * 管理员用户名
     */
    private String username;

    /**
     * 管理员密码
     */
    private String password;

    /**
     * 管理员邮箱
     */
    private String email;

}