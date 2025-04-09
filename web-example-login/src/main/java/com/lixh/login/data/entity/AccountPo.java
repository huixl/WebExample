package com.lixh.login.data.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户账户实体类
 * @author lixionghui
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_account")
public class AccountPo extends BasePo {

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 地址
     */
    private String address;

    /**
     * 国家
     */
    private String country;

    /**
     * 城市
     */
    private String city;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 账户状态 0-正常 1-锁定 2-禁用
     */
    private Integer accountStatus;

    /**
     * 验证状态 0-未验证 1-已验证
     */
    private Integer verificationStatus;

    /**
     * 实名认证类型：ID_CARD-身份证，PASSPORT-护照，OTHER-其他
     */
    private String verificationType;

    /**
     * 实名认证证件号码
     */
    private String verificationId;

    /**
     * 实名认证时间
     */
    private LocalDateTime verifiedAt;

    /**
     * 账户锁定原因
     */
    private String lockReason;

    /**
     * 账户锁定时间
     */
    private LocalDateTime lockedAt;

    /**
     * 账户锁定过期时间
     */
    private LocalDateTime lockExpiresAt;

    @TableField(exist = false)
    private LoginPo loginPo;
} 