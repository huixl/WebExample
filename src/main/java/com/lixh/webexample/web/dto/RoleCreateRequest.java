package com.lixh.webexample.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建请求
 */
@Data
public class RoleCreateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    private String code;

    /**
     * 角色描述
     */
    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status = 1;
}