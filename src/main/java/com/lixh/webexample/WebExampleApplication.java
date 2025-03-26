package com.lixh.webexample;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.lixh.webexample")
@MapperScan("com.lixh.webexample.data.mapper")
public class WebExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebExampleApplication.class, args);
        log.info("WebExample Application started");
    }

}
