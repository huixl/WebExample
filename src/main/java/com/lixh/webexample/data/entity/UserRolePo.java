package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-角色关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_perm_user_role")
public class UserRolePo extends BasePo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;
}