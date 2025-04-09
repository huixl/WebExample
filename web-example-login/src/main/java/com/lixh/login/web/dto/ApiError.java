package com.lixh.login.web.dto;

import lombok.Getter;

@Getter
public enum ApiError {
    // 系统级错误 (1xxxxx)
    SYSTEM_ERROR("100000", "系统内部错误"),
    SERVICE_UNAVAILABLE("100001", "服务暂时不可用"),
    GATEWAY_ERROR("100002", "网关错误"),

    // 认证和授权错误 (2xxxxx)
    UNAUTHORIZED("200000", "未经授权的访问"),
    FORBIDDEN("200001", "禁止访问"),
    INVALID_TOKEN("200002", "无效的访问令牌"),
    TOKEN_EXPIRED("200003", "访问令牌已过期"),

    // 参数验证错误 (3xxxxx)
    INVALID_PARAMETER("300000", "无效的参数"),
    MISSING_PARAMETER("300001", "缺少必需的参数"),
    INVALID_FORMAT("300002", "无效的数据格式"),
    ACCOUNT_PASSWORD_ERROR("300003", "账号或密码错误"),

    // 业务逻辑错误 (4xxxxx)
    RESOURCE_NOT_FOUND("400000", "请求的资源不存在"),
    DUPLICATE_RESOURCE("400001", "资源已存在"),
    OPERATION_FAILED("400002", "操作失败"),
    DATA_CONFLICT("400003", "数据冲突"),

    // 外部服务错误 (5xxxxx)
    EXTERNAL_SERVICE_ERROR("500000", "外部服务调用失败"),
    EXTERNAL_SERVICE_TIMEOUT("500001", "外部服务调用超时"),
    EXTERNAL_SERVICE_INVALID_RESPONSE("500002", "外部服务返回无效响应");

    private final String code;

    private final String message;

    ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}