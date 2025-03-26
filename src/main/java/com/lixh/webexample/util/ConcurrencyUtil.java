package com.lixh.webexample.util;

import com.lixh.webexample.config.ConcurrencyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * 并发控制工具类
 */
@Component
@Slf4j
public class ConcurrencyUtil {

    private final ConcurrencyConfig concurrencyConfig;
    private final Executor taskExecutor;

    public ConcurrencyUtil(ConcurrencyConfig concurrencyConfig, Executor taskExecutor) {
        this.concurrencyConfig = concurrencyConfig;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 使用信号量和 CountDownLatch 控制并发执行任务
     *
     * @param items           要处理的项目列表
     * @param task            处理每个项目的任务
     * @param concurrencyType 并发类型，用于确定使用哪个并发配置
     * @param <T>             项目类型
     * @throws InterruptedException 如果等待过程中被中断
     */
    public <T> void executeWithConcurrencyControl(List<T> items, Consumer<T> task, ConcurrencyType concurrencyType)
            throws InterruptedException {
        if (items == null || items.isEmpty()) {
            log.info("没有项目需要处理");
            return;
        }

        // 根据并发类型获取并发数量
        int concurrency = getConcurrencyByType(concurrencyType);

        // 创建信号量和 CountDownLatch
        Semaphore semaphore = new Semaphore(concurrency);
        CountDownLatch latch = new CountDownLatch(items.size());

        log.info("开始并发处理 {} 个项目，并发数量: {}", items.size(), concurrency);

        // 并发处理每个项目
        for (T item : items) {
            // 获取信号量许可
            semaphore.acquire();

            // 提交任务到线程池
            taskExecutor.execute(() -> {
                try {
                    // 执行任务
                    task.accept(item);
                } catch (Exception e) {
                    log.error("处理项目出错: {}", e.getMessage(), e);
                } finally {
                    // 释放信号量许可
                    semaphore.release();
                    // 减少计数
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        latch.await();
        log.info("所有项目处理完成");
    }

    /**
     * 根据并发类型获取并发数量
     *
     * @param concurrencyType 并发类型
     * @return 并发数量
     */
    private int getConcurrencyByType(ConcurrencyType concurrencyType) {
        // 目前只有 AI_INFERENCE 类型，直接返回对应的配置值
        return concurrencyConfig.getAiInferenceConcurrency();
    }

    /**
     * 并发类型枚举
     */
    public enum ConcurrencyType {
        /**
         * AI 推断
         */
        AI_INFERENCE
    }
}