package com.lixh.webexample.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色ID请求
 */
@Data
public class RoleIdRequest {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long id;
}