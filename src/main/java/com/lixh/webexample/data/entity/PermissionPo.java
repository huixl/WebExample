package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_perm_permission")
public class PermissionPo extends BaseEntity {

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 资源类型：MENU-菜单，BUTTON-按钮，API-接口，DATA-数据
     */
    private String resourceType;

    /**
     * 资源路径
     */
    private String resourcePath;

    /**
     * 所属权限包ID
     */
    private Long packageId;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}