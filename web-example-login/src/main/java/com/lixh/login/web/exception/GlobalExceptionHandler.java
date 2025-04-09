package com.lixh.login.web.exception;

import com.lixh.login.exception.BusinessException;
import com.lixh.login.web.dto.ApiError;
import com.lixh.login.web.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ApiResponse.error(ApiError.SYSTEM_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> BusinessException(Exception e) {
        return ApiResponse.error(ApiError.ACCOUNT_PASSWORD_ERROR.getCode(),e.getMessage());
    }


    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ApiResponse<Void> handleValidationException(Exception e) {
        log.error("Validation exception", e);
        return ApiResponse.error(ApiError.INVALID_PARAMETER);
    }
}