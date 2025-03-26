package com.lixh.webexample.service;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fewshot服务接口
 * 负责存储和检索fewshot示例
 */
public interface FewshotService {

    /**
     * 存储fewshot示例
     *
     * @param parseFieldValue 待解析文本
     * @param extractedFields 解析结果
     * @return 异步执行结果，包含操作是否成功的布尔值
     */
    CompletableFuture<Boolean> storeFewshotExample(String parseFieldValue, Map<String, String> extractedFields);

    /**
     * 检索最相似的fewshot示例
     *
     * @param parseFieldValue 待解析文本
     * @param limit           限制返回的示例数量
     * @return fewshot示例列表
     */
    List<FewshotExample> findSimilarExamples(String parseFieldValue, int limit);

    /**
     * Fewshot示例类
     */
    @Getter
    class FewshotExample {
        private final String parseFieldValue;
        private final Map<String, String> extractedFields;

        public FewshotExample(String parseFieldValue, Map<String, String> extractedFields) {
            this.parseFieldValue = parseFieldValue;
            this.extractedFields = extractedFields;
        }

    }
}