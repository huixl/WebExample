package com.lixh.login.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色ID列表请求
 */
@Data
public class RoleIdsRequest {

    /**
     * 角色ID列表
     */
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}