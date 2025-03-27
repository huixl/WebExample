package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserPo extends BasePo {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后的）
     */
    private String password;

    /**
     * 密码盐值
     */
    private String salt;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private java.time.LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;
}