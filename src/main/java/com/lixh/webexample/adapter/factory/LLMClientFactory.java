package com.lixh.webexample.adapter.factory;

import com.lixh.webexample.adapter.LLMClient;

import java.util.Map;

/**
 * LLM客户端工厂接口
 */
public interface LLMClientFactory {

    /**
     * 创建LLM客户端
     *
     * @param type   客户端类型
     * @param params 额外参数
     * @return LLM客户端实例
     */
    LLMClient createClient(String type, Map<String, Object> params);

    /**
     * 获取支持的客户端类型
     *
     * @return 支持的客户端类型列表
     */
    boolean supportsClientType(String type);
}