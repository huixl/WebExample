package com.lixh.webexample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "external-service")
public class ExternalServiceConfig {

    private BaichuanConfig baichuan;

    private SiliconFlowConfig siliconflow;

    @Data
    public static class BaichuanConfig {

        private String modelUrl;

        private String embeddingUrl;

        private String sk;
    }

    @Data
    public static class SiliconFlowConfig {

        private String modelUrl;

        private String apiKey;

        private String model;
    }
}