package com.lixh.webexample.dto.response;

import lombok.Data;

/**
 * 基础响应DTO
 */
@Data
public class BaseResponse {

    private Boolean success;

    private String message;

    /**
     * 创建成功响应
     *
     * @param message 成功消息
     * @return 成功响应
     */
    public static BaseResponse success(String message) {
        BaseResponse response = new BaseResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param message 失败消息
     * @return 失败响应
     */
    public static BaseResponse error(String message) {
        BaseResponse response = new BaseResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}