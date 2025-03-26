package com.lixh.webexample.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lixh.webexample.config.ExternalServiceConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SiliconFlowModelClient implements LLMClient {

    private final Map<String, Object> params;

    private final OkHttpClient client;

    @Autowired
    private ExternalServiceConfig externalServiceConfig;

    public SiliconFlowModelClient(ExternalServiceConfig externalServiceConfig, Map<String, Object> params) {
        this.externalServiceConfig = externalServiceConfig;
        this.params = params;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        init();
    }

    @PostConstruct
    public void init() {
        if (!StringUtils.isNotBlank(externalServiceConfig.getSiliconflow().getModelUrl())) {
            throw new IllegalStateException("SiliconFlow模型URL未配置");
        }
        if (!StringUtils.isNotBlank(externalServiceConfig.getSiliconflow().getApiKey())) {
            throw new IllegalStateException("SiliconFlow API Key未配置");
        }
        log.info("SiliconFlow模型客户端初始化完成，API地址: {}", externalServiceConfig.getSiliconflow().getModelUrl());
    }

    @Override
    public void streamChat(List<Map<String, String>> messages, StreamResponseCallback callback) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        // 添加额外参数
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    requestBody.put(key, value);
                }
            });
        }

        // 如果 "model" 不存在，则添加默认模型
        if (StringUtils.isBlank(requestBody.getString("model"))) {
            requestBody.put("model", externalServiceConfig.getSiliconflow().getModel());
        }

        Request request = new Request.Builder()
                .url(externalServiceConfig.getSiliconflow().getModelUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + externalServiceConfig.getSiliconflow().getApiKey())
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toJSONString()))
                .build();

        log.debug("正在发送请求到SiliconFlow API: {}", requestBody);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String errorBody = response.body() != null ? response.body().string() : "空";
                log.error("请求失败。状态码: {}, 错误信息: {}", response.code(), errorBody);
                throw new RuntimeException("API调用失败: 状态码=" + response.code() + ", 错误信息=" + errorBody);
            }

            try (ResponseBody responseBody = response.body()) {

                BufferedSource source = responseBody.source();
                while (!source.exhausted()) {
                    String line = source.readUtf8Line();
                    if (line == null || line.isEmpty()) {
                        continue;
                    }

                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            log.debug("流式响应完成");
                            break;
                        }
                        try {
                            JSONObject jsonData = JSON.parseObject(data);
                            callback.onData(jsonData);
                        } catch (Exception e) {
                            log.error("解析响应数据失败: {}", data, e);
                            callback.onError(e);
                            throw new RuntimeException("解析响应数据时发生错误", e);
                        }
                    }
                }
                callback.onComplete();
            }
        } catch (IOException e) {
            log.error("调用SiliconFlow API时发生异常", e);
            callback.onError(e);
            throw new RuntimeException("API调用异常", e);
        }
    }
}