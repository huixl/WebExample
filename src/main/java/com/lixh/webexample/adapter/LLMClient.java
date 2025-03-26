package com.lixh.webexample.adapter;

import java.util.List;
import java.util.Map;

/**
 * 通用LLM客户端接口
 */
public interface LLMClient {

    /**
     * 流式对话接口
     *
     * @param messages 对话历史，格式为List<Map<String, String>>，每个Map包含role和content
     * @param callback 流式回调接口
     */
    void streamChat(List<Map<String, String>> messages, StreamResponseCallback callback);

    /**
     * 流式响应回调接口
     */
    interface StreamResponseCallback {

        /**
         * 收到数据块时的回调
         *
         * @param data 数据块
         */
        void onData(Object data);

        /**
         * 发生错误时的回调
         *
         * @param e 异常
         */
        void onError(Exception e);

        /**
         * 完成时的回调
         */
        void onComplete();
    }
}