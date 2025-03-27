package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录方式字典实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_login_type")
public class LoginTypePo extends BasePo {

    /**
     * 登录方式代码
     */
    private String typeCode;

    /**
     * 登录方式名称
     */
    private String typeName;

    /**
     * 最大尝试限制
     */
    private Integer maxAttempts;

    /**
     * 是否启用 0-启用 1-禁用
     */
    private Integer isEnabled;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
} 