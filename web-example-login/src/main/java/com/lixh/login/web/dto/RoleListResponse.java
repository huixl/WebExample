package com.lixh.login.web.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色列表响应
 */
@Data
public class RoleListResponse {

    /**
     * 角色列表
     */
    private List<RoleResponse> roles;
}