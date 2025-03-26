package com.lixh.webexample.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

@Slf4j
@ControllerAdvice
public class LoggingRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(@NotNull MethodParameter methodParameter, @NotNull Type targetType,
            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @NotNull
    @Override
    public Object afterBodyRead(@NotNull Object body, @NotNull HttpInputMessage inputMessage,
            @NotNull MethodParameter parameter, @NotNull Type targetType,
            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        // log.info("Request Body: {}", body);
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}