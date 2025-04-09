package com.lixh.login.config;

import com.alibaba.fastjson.JSON;
import com.lixh.login.web.dto.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 自定义HTTP消息转换器，用于统一响应格式
 * 注意：此转换器仅用于处理响应，不处理请求
 * <p>
 * 该转换器的主要作用是将控制器返回的非ApiResponse类型对象包装成ApiResponse对象，
 * 以实现API响应格式的统一。它不会处理请求体的读取，也不会处理已经是ApiResponse类型的响应。
 */
public class ApiResponseHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public ApiResponseHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    @Override
    public boolean canRead(@NotNull Class<?> clazz, MediaType mediaType) {
        // 禁用读取功能，确保此转换器不会被用于读取请求
        return false;
    }

    @Override
    protected boolean canRead(@NotNull MediaType mediaType) {
        // 禁用读取功能，确保此转换器不会被用于读取请求
        return false;
    }

    @Override
    protected boolean supports(@NotNull Class<?> clazz) {
        // 不处理String和ApiResponse类型
        // 处理所有其他类型，包括request，dto，response等
        return !String.class.equals(clazz) && !ApiResponse.class.isAssignableFrom(clazz);
    }

    @NotNull
    @Override
    protected Object readInternal(@NotNull Class<?> clazz, @NotNull HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {
        // 此转换器不应该被用于读取请求
        // 如果尝试使用此转换器读取请求，将抛出异常
        throw new UnsupportedOperationException("此转换器仅用于处理响应，不处理请求");
    }

    @Override
    protected void writeInternal(@NotNull Object obj, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // 将响应对象包装为统一的ApiResponse格式
        // 这是转换器的核心功能：将任何非ApiResponse对象包装成ApiResponse对象
        ApiResponse<Object> response = ApiResponse.success(obj);
        response.setTimestamp(Instant.now().toEpochMilli());
        String json = JSON.toJSONString(response);
        outputMessage.getBody().write(json.getBytes(StandardCharsets.UTF_8));
    }
}