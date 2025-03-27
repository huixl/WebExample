package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_perm_role_permission")
public class RolePermissionPo extends BasePo {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;
}