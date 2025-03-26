package com.lixh.webexample.dto.request;

import lombok.Data;

/**
 * 继续AI推断请求
 */
@Data
public class ContinueAiInferenceRequest {

    /**
     * 用户输入
     */
    private String userInput;
} 