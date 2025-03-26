package com.lixh.webexample.adapter.dto;

import com.alibaba.fastjson.JSONObject;
import com.lixh.webexample.adapter.LLMClient.StreamResponseCallback;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class SiliconFlowDataChunkCallback implements StreamResponseCallback {

    private final StringBuilder contentBuilder;

    private final StringBuilder reasoningBuilder;

    private final CountDownLatch countDownLatch;

    public SiliconFlowDataChunkCallback() {
        this.contentBuilder = new StringBuilder();
        this.reasoningBuilder = new StringBuilder();
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void onData(Object data) {
        if (data instanceof JSONObject jsonData) {
            try {
                JSONObject choices = jsonData.getJSONArray("choices").getJSONObject(0);
                JSONObject delta = choices.getJSONObject("delta");
                String content = delta.getString("content");

                // 处理普通内容
                if (content != null) {
                    contentBuilder.append(content);
                }

                // 处理推理内容（如果有）
                String reasoningContent = delta.getString("reasoning_content");
                if (reasoningContent != null) {
                    reasoningBuilder.append(reasoningContent);
                }

                // 检查是否完成
                String finishReason = choices.getString("finish_reason");
                if ("stop".equals(finishReason) || "length".equals(finishReason)) {
                    countDownLatch.countDown();
                }
            } catch (Exception e) {
                log.error("解析SiliconFlow响应数据失败: {}", jsonData, e);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("处理SiliconFlow流式响应时发生错误", e);
        countDownLatch.countDown();
    }

    @Override
    public void onComplete() {
        countDownLatch.countDown();
    }

    /**
     * 获取模型生成的内容
     */
    public String getContent() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("等待响应完成时发生错误", e);
        }
        return contentBuilder.toString();
    }

    /**
     * 获取模型的推理过程（如果有）
     */
    public String getReasoningContent() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("等待响应完成时发生错误", e);
        }
        return reasoningBuilder.toString();
    }
}