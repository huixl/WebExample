package com.lixh.webexample.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lixh.webexample.service.ai.ParserAssistant;
import com.lixh.webexample.service.freemarker.FreeMarkerService;
import com.lixh.webexample.tools.GasketPredictionTool;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;

/**
 * ParserAssistant配置类
 */
@Configuration
public class ParserAssistantConfig {

    private final Gson gson = new Gson();

    /**
     * 创建ParserAssistant Bean
     *
     * @param externalServiceConfig 外部服务配置
     * @param freeMarkerService     FreeMarker服务
     * @param gasketPredictionTool  垫片预测工具
     * @return ParserAssistant实例
     */
    @Bean
    public ParserAssistant parserAssistant(ExternalServiceConfig externalServiceConfig,
                                           FreeMarkerService freeMarkerService,
                                           GasketPredictionTool gasketPredictionTool) {
        // 创建OpenAiChatModel实例
        final OpenAiChatModel openAiLike = OpenAiChatModel.builder()
                .baseUrl(externalServiceConfig.getSiliconflow().getModelUrl())
                .apiKey(externalServiceConfig.getSiliconflow().getApiKey())
                .modelName(externalServiceConfig.getSiliconflow().getModel())
                .maxRetries(5)
                .logRequests(true)
                .logResponses(true)
                .build();

        // 创建工具提供者，动态注册垫片预测工具
        ToolProvider toolProvider = new ToolProvider() {
            @Override
            public ToolProviderResult provideTools(ToolProviderRequest request) {
                // 获取用户消息
                String userMessage = request.userMessage().singleText();
                
                // 检查用户消息是否包含垫片预测相关的关键词
                if (userMessage.contains("垫片")) {
                    
                    // 获取垫片预测工具的规范
                    ToolSpecification toolSpecification = ToolSpecification.builder()
                        .name("predictValue")
                        .description("根据已知字段值预测目标字段的可能值，帮助用户填写垫片信息")
                        .parameters(JsonObjectSchema.builder()
                            .properties(Map.of(
                                "knownFieldsStr", JsonStringSchema.builder().description("已知字段值字符串，格式：'fieldName1=已知值1;fieldName2=已知值2'").build(),
                                "predictFieldName", JsonStringSchema.builder().description("需要预测的字段名称").build()
                            ))
                            .build())
                        .build();
                    
                    if (toolSpecification != null) {
                        // 创建工具执行器
                        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
                            try {
                                // 解析工具执行请求的参数
                                String argumentsJson = toolExecutionRequest.arguments();
                                // 使用GSON解析JSON
                                JsonObject jsonObject = JsonParser.parseString(argumentsJson).getAsJsonObject();
                                String knownFieldsStr = getStringFromJson(jsonObject, "knownFieldsStr");
                                String predictFieldName = getStringFromJson(jsonObject, "predictFieldName");
                                
                                // 调用垫片预测工具
                                List<String> result = gasketPredictionTool.predictValue(knownFieldsStr, predictFieldName);
                                
                                // 将结果转换为字符串
                                return String.join("\n", result);
                            } catch (Exception e) {
                                return "预测失败：" + e.getMessage();
                            }
                        };
                        
                        // 返回工具提供者结果
                        return ToolProviderResult.builder()
                            .add(toolSpecification, toolExecutor)
                            .build();
                    }
                }
                
                // 如果不需要垫片预测工具，返回空结果
                return ToolProviderResult.builder().build();
            }
        };

        // 创建ParserAssistant实例
        return AiServices.builder(ParserAssistant.class)
                .chatLanguageModel(openAiLike)
                .systemMessageProvider(memoryId -> getSystemPrompt(freeMarkerService))
                .toolProvider(toolProvider)
                .build();
    }

    /**
     * 获取系统提示
     *
     * @param freeMarkerService FreeMarker服务
     * @return 系统提示
     */
    private String getSystemPrompt(FreeMarkerService freeMarkerService) {
        // 使用一个简单的模板作为系统提示
        Map<String, Object> templateParams = new HashMap<>();
        return freeMarkerService.processTemplate("templates/prompts/field-parser-system.ftl", templateParams);
    }

    /**
     * 从JSON对象中获取字符串值
     * 
     * @param jsonObject JSON对象
     * @param key 键名
     * @return 字符串值，如果不存在则返回空字符串
     */
    private String getStringFromJson(JsonObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            JsonElement element = jsonObject.get(key);
            if (element != null && !element.isJsonNull()) {
                return element.getAsString();
            }
        }
        return "";
    }
} 