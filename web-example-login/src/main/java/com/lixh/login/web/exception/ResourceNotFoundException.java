package com.lixh.login.web.exception;

/**
 * 资源未找到异常
 * @author lixionghui
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}