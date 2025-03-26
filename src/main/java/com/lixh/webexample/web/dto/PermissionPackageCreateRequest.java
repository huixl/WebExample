package com.lixh.webexample.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 权限包创建请求
 */
@Data
public class PermissionPackageCreateRequest {

    /**
     * 权限包名称
     */
    @NotBlank(message = "权限包名称不能为空")
    private String name;

    /**
     * 权限包编码
     */
    @NotBlank(message = "权限包编码不能为空")
    private String code;

    /**
     * 权限包描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status = 1;
}