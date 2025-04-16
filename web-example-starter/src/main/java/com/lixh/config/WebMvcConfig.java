package com.lixh.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * @author lixionghui
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //List<String> pathList = Arrays.asList("/");
    //
    //@Override
    //public void configurePathMatch(PathMatchConfigurer configurer) {
    //    // 将"/"请求映射到"/api"
    //    configurer.addPathPrefix("/api", path -> pathList.contains(path));
    //}
} 