package com.lixh.webexample.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 解析助手接口
 * 负责从未处理数据中解析字段，并支持垫片预测
 */
public interface ParserAssistant {

    /**
     * 解析字段
     *
     * @param userMessage 用户消息
     * @param memoryId    记忆ID
     * @return 解析结果
     */
    @SystemMessage("你是一个专业的解析助手，可以从未处理数据中解析字段。" +
                  "你还可以使用垫片预测工具，根据已知的字段值预测其他字段的可能值。" +
                  "当用户询问垫片预测相关问题时，你应该使用垫片预测工具来帮助用户。" +
                  "垫片预测工具需要两个参数：已知字段值字符串和需要预测的字段名称。" +
                  "已知字段值字符串的格式为：'fieldName1=已知值1;fieldName2=已知值2'。" +
                  "回答时请使用中文，并保持专业、简洁的风格。")
    String parse(@UserMessage String userMessage, @MemoryId String memoryId);
} 