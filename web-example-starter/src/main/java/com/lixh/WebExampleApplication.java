package com.lixh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lixionghui
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.lixh")
@MapperScan("com.lixh.login.data.mapper")
public class WebExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebExampleApplication.class, args);
        log.info("WebExample Application started");
    }

}
