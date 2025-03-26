package com.lixh.webexample.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新请求
 */
@Data
public class RoleUpdateRequest {

    /**
     * 角色名称
     */
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;

    /**
     * 角色描述
     */
    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}