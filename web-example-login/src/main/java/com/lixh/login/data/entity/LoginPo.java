package com.lixh.login.data.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户登录实体类
 * @author lixionghui
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_login")
public class LoginPo extends BasePo {

    /**
     * 认证标识：邮箱、手机号、用户名
     */
    private String identity;

    /**
     * 认证凭证：密码、验证码
     */
    private String credential;

    /**
     * 登录方式ID
     */
    private Long loginTypeId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 登录尝试次数
     */
    private Integer loginAttempts;

    /**
     * 账户是否锁定 0-未锁定 1-已锁定
     */
    private Integer accountLocked;

    /**
     * 关联的账户ID
     */
    private Long accountId;
} 