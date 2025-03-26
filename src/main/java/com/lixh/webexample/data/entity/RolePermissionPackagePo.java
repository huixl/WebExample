package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限包关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_perm_role_permission_package")
public class RolePermissionPackagePo extends BaseEntity {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限包ID
     */
    private Long packageId;
}