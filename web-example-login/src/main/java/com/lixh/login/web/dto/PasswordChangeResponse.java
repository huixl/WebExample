package com.lixh.login.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码修改响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;
}