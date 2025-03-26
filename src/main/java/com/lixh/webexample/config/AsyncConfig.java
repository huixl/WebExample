package com.lixh.webexample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 异步任务配置
 */
@Configuration
public class AsyncConfig {

    @Bean
    @Primary
    public Executor taskExecutor() {
        // 使用工作窃取模式的ForkJoinPool，提高执行效率
        ForkJoinPoolFactoryBean factoryBean = new ForkJoinPoolFactoryBean();
        // 设置并行度，默认为CPU核心数,
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        factoryBean.setParallelism(availableProcessors * 3 / 4); // 设置并行度为CPU核心数的3/4
        // 启用异步模式
        factoryBean.setAsyncMode(true);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * 保留原有线程池配置作为备选
     */
    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("ParseTask-");
        executor.initialize();
        return executor;
    }
}