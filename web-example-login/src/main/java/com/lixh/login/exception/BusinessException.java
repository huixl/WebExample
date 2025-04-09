package com.lixh.login.exception;

/**
 * 业务异常类
 * @author lixionghui
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer errorCode;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer errorCode,String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
} 