package com.lixh.login.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限包实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_perm_permission_package")
public class PermissionPackagePo extends BasePo {

    /**
     * 权限包名称
     */
    private String name;

    /**
     * 权限包编码
     */
    private String code;

    /**
     * 权限包描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}