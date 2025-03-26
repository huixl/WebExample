package com.lixh.webexample.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.lixh.webexample.mapper")
public class MyBatisConfig {

}