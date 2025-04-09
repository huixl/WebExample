package com.lixh.login.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * @author lixionghui
 */
@Data
public class LoginRequest {

    /**
     * 登陆标识
     */
    @NotBlank(message = "账号不能为空")
    private String identity;

    /**
     * 登陆凭证
     */
    @NotBlank(message = "密码不能为空")
    private String credential;

    /**
     * 登陆方式
     */
    @NotBlank(message = "登陆方式不能为空")
    private String loginType;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;
}