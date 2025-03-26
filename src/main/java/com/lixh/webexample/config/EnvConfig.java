package com.lixh.webexample.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource(value = "file:.env", ignoreResourceNotFound = true)
})
public class EnvConfig {
    // 配置类不需要其他内容
}