package com.lixh.login.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户角色分配请求
 */
@Data
public class UserRoleAssignRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色ID列表
     */
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}