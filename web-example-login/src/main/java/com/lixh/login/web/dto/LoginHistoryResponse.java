package com.lixh.login.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录历史响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryResponse {

    /**
     * 登录ID
     */
    private Long id;

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