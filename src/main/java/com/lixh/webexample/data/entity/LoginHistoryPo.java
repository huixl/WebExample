package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 登录历史实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_login_history")
public class LoginHistoryPo extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 登录状态：0-失败，1-成功
     */
    private Integer loginStatus;

    /**
     * 登录类型：PASSWORD-密码登录，TOKEN-令牌登录
     */
    private String loginType;

    /**
     * 备注
     */
    private String remark;
}