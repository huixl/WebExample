package com.lixh.webexample.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码验证请求DTO
 */
@Data
public class PasswordVerifyRequest {

    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;
}