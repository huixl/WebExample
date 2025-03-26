package com.lixh.webexample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.lixh.example")
public class WebExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebExampleApplication.class, args);
        log.info("WebExample Application started");
    }

}
