package com.lixh.login.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户ID请求
 */
@Data
public class UserIdRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;
}