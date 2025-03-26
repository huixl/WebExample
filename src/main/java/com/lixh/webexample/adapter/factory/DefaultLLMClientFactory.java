package com.lixh.webexample.adapter.factory;

import com.lixh.webexample.adapter.BaichuanModelClient;
import com.lixh.webexample.adapter.LLMClient;
import com.lixh.webexample.adapter.SiliconFlowModelClient;
import com.lixh.webexample.config.ExternalServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultLLMClientFactory implements LLMClientFactory {

    public static final String SILICON_FLOW = "siliconflow";

    public static final String BAICHUAN = "baichuan";

    @Autowired
    private ExternalServiceConfig externalServiceConfig;

    @Override
    public LLMClient createClient(String type, Map<String, Object> params) {
        return switch (type.toLowerCase()) {
            case SILICON_FLOW -> createSiliconFlowClient(params);
            case BAICHUAN -> createBaichuanClient(params);
            default -> throw new IllegalArgumentException("不支持的LLM客户端类型: " + type);
        };
    }

    @Override
    public boolean supportsClientType(String type) {
        return SILICON_FLOW.equalsIgnoreCase(type) || BAICHUAN.equalsIgnoreCase(type);
    }

    private LLMClient createSiliconFlowClient(Map<String, Object> params) {
        return new SiliconFlowModelClient(externalServiceConfig, params);
    }

    private LLMClient createBaichuanClient(Map<String, Object> params) {
        return new BaichuanModelClient(externalServiceConfig, params);
    }
}