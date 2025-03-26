package com.lixh.webexample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 并发控制配置
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "orderiq.concurrency")
public class ConcurrencyConfig {

    /**
     * AI 推断并发数量
     */
    private int aiInferenceConcurrency = 5;

}