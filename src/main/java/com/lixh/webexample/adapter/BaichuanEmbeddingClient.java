package com.lixh.webexample.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lixh.webexample.adapter.dto.EmbeddingResponse;
import com.lixh.webexample.config.ExternalServiceConfig;
import com.lixh.webexample.ex.SystemException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BaichuanEmbeddingClient {

    // Redis缓存前缀和过期时间
    private static final String CACHE_PREFIX = "embedding:baichuan:";
    private static final long CACHE_EXPIRATION = 7 * 24 * 60 * 60; // 7天过期时间
    private final OkHttpClient client;
    private final ExternalServiceConfig externalServiceConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    public BaichuanEmbeddingClient(ExternalServiceConfig externalServiceConfig,
                                   RedisTemplate<String, Object> redisTemplate) {
        this.client = new OkHttpClient();
        this.externalServiceConfig = externalServiceConfig;
        this.redisTemplate = redisTemplate;
    }

    public List<EmbeddingResponse.Data> getEmbeddings(List<String> inputs) {
        if (inputs.size() <= 16) {
            return getEmbeddingsWithCache(inputs);
        }

        List<CompletableFuture<List<EmbeddingResponse.Data>>> futures = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i += 16) {
            List<String> batch = inputs.subList(i, Math.min(i + 16, inputs.size()));
            CompletableFuture<List<EmbeddingResponse.Data>> future = CompletableFuture
                    .supplyAsync(() -> getEmbeddingsWithCache(batch)).exceptionally(e -> {
                        log.error("Failed to fetch embeddings", e);
                        return Collections.emptyList();
                    });
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }

        List<EmbeddingResponse.Data> result = new ArrayList<>();
        for (CompletableFuture<List<EmbeddingResponse.Data>> future : futures) {
            try {
                result.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new SystemException(e);
            }
        }

        return result;
    }

    /**
     * 带缓存的获取向量方法
     *
     * @param inputs 输入文本列表
     * @return 向量结果列表
     */
    private List<EmbeddingResponse.Data> getEmbeddingsWithCache(List<String> inputs) {
        // 检查缓存中是否有所有输入的向量
        List<EmbeddingResponse.Data> cachedResults = new ArrayList<>();
        List<String> uncachedInputs = new ArrayList<>();
        List<Integer> uncachedIndices = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            String input = inputs.get(i);
            String cacheKey = getCacheKey(input);
            Object cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                // 从缓存中获取向量
                EmbeddingResponse.Data cachedData = JSON.parseObject(cached.toString(), EmbeddingResponse.Data.class);
                cachedData.setIndex(i); // 设置正确的索引
                cachedResults.add(cachedData);
                log.debug("Cache hit for input: {}", input);
            } else {
                // 记录未缓存的输入和索引
                uncachedInputs.add(input);
                uncachedIndices.add(i);
            }
        }

        // 如果所有输入都已缓存，直接返回缓存结果
        if (uncachedInputs.isEmpty()) {
            return cachedResults;
        }

        // 获取未缓存的向量
        List<EmbeddingResponse.Data> fetchedResults = fetchEmbeddings(uncachedInputs);

        // 缓存新获取的向量
        for (int i = 0; i < fetchedResults.size(); i++) {
            EmbeddingResponse.Data data = fetchedResults.get(i);
            String input = uncachedInputs.get(i);
            String cacheKey = getCacheKey(input);

            // 缓存向量
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data), CACHE_EXPIRATION, TimeUnit.SECONDS);
            log.debug("Cached embedding for input: {}", input);

            // 设置正确的索引
            data.setIndex(uncachedIndices.get(i));
        }

        // 合并缓存结果和新获取的结果
        List<EmbeddingResponse.Data> allResults = new ArrayList<>(cachedResults);
        allResults.addAll(fetchedResults);

        // 按索引排序
        allResults.sort(Comparator.comparingInt(EmbeddingResponse.Data::getIndex));

        return allResults;
    }

    public List<SimilarityResult> getSimilarityWithIndex(String input, List<String> otherInputs) {
        List<String> allInputs = new ArrayList<>(otherInputs);
        allInputs.add(0, input); // Ensure the input is the first element

        List<EmbeddingResponse.Data> embeddings = getEmbeddings(allInputs);

        if (embeddings.isEmpty()) {
            throw new SystemException("Failed to fetch embeddings");
        }

        List<Double> inputEmbedding = embeddings.get(0).getEmbedding();
        List<List<Double>> otherEmbeddings = embeddings.subList(1, embeddings.size()).stream()
                .map(EmbeddingResponse.Data::getEmbedding)
                .toList();

        List<SimilarityResult> similarities = new ArrayList<>();
        for (int i = 0; i < otherEmbeddings.size(); i++) {
            similarities.add(new SimilarityResult(i, cosineSimilarity(inputEmbedding, otherEmbeddings.get(i)),
                    otherInputs.get(i)));
        }

        return similarities;
    }

    private List<EmbeddingResponse.Data> fetchEmbeddings(List<String> inputs) {
        JSONObject requestBodyMap = new JSONObject();
        requestBodyMap.put("model", "Baichuan-Text-Embedding");
        requestBodyMap.put("input", inputs);

        String jsonString = requestBodyMap.toJSONString();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(jsonString, mediaType);

        Request request = new Request.Builder()
                .url(externalServiceConfig.getBaichuan().getEmbeddingUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + externalServiceConfig.getBaichuan().getSk())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return JSON.parseObject(Objects.requireNonNull(response.body()).string(), EmbeddingResponse.class)
                    .getData();
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    private double cosineSimilarity(List<Double> vecA, List<Double> vecB) {
        // sanity check
        if (vecA.size() != vecB.size()) {
            throw new IllegalArgumentException("Vector sizes do not match");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vecA.size(); i++) {
            dotProduct += vecA.get(i) * vecB.get(i);
            normA += Math.pow(vecA.get(i), 2);
            normB += Math.pow(vecB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 生成缓存键
     *
     * @param input 输入文本
     * @return 缓存键
     */
    private String getCacheKey(String input) {
        // 使用MD5哈希作为缓存键，避免特殊字符问题
        String md5Hash = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        return CACHE_PREFIX + md5Hash;
    }

    @Data
    public static class SimilarityResult {

        private int originalIndex;

        private double similarity;

        private String originalInput;

        public SimilarityResult() {
        }

        public SimilarityResult(int originalIndex, double similarity, String originalInput) {
            this.originalIndex = originalIndex;
            this.similarity = similarity;
            this.originalInput = originalInput;
        }
    }
}
