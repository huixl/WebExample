package com.lixh.webexample.adapter.dto;

import com.alibaba.fastjson.JSONObject;
import com.lixh.webexample.adapter.LLMClient.StreamResponseCallback;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class BaichuanDataChunkCallback implements StreamResponseCallback {

    private final StringBuilder stringBuilder;

    private final CountDownLatch countDownLatch;

    public BaichuanDataChunkCallback() {
        this.stringBuilder = new StringBuilder();
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void onData(Object data) {
        if (data instanceof JSONObject jsonData) {
            BaichuanStreamDataChunk baichuanStreamDataChunk = jsonData.toJavaObject(BaichuanStreamDataChunk.class);
            String content = baichuanStreamDataChunk.getChoices().get(0).getDelta().getContent();
            stringBuilder.append(content);
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("Error occurred while processing stream", e);
    }

    @Override
    public void onComplete() {
        countDownLatch.countDown();
    }

    public String getData() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("Error occurred while waiting for stream to complete", e);
        }
        return stringBuilder.toString();
    }
}
