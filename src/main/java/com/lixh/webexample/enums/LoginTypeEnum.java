package com.lixh.webexample.enums;


/**
 * 登录方式枚举
 * @author lixionghui
 */
public enum LoginTypeEnum {
    
    USERNAME_PASSWORD("username_password", "用户名密码登录"),
    EMAIL_CODE("email_code", "邮箱验证码登录"),
    PHONE_CODE("phone_code", "手机验证码登录"),
    WECHAT("wechat", "微信登录"),
    GITHUB("github", "GitHub登录"),
    GOOGLE("google", "Google登录");

    private final String code;
    private final String desc;

    LoginTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static LoginTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LoginTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 