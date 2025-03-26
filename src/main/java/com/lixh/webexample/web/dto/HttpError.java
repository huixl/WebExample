package com.lixh.webexample.web.dto;

import lombok.Getter;

@Getter
public enum HttpError {
    // 4xx Client Errors
    BAD_REQUEST("400", "请求参数错误"),
    UNAUTHORIZED("401", "未经授权"),
    FORBIDDEN("403", "禁止访问"),
    NOT_FOUND("404", "资源不存在"),
    METHOD_NOT_ALLOWED("405", "请求方法不允许"),
    CONFLICT("409", "资源冲突"),
    PRECONDITION_FAILED("412", "前置条件失败"),
    UNSUPPORTED_MEDIA_TYPE("415", "不支持的媒体类型"),
    TOO_MANY_REQUESTS("429", "请求过于频繁"),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR("500", "服务器内部错误"),
    NOT_IMPLEMENTED("501", "功能未实现"),
    BAD_GATEWAY("502", "网关错误"),
    SERVICE_UNAVAILABLE("503", "服务不可用"),
    GATEWAY_TIMEOUT("504", "网关超时");

    private final String code;

    private final String message;

    HttpError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
