package com.lixh.login.config.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * 使用此注解标记的方法会在执行前进行权限检查
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCheck {

    /**
     * 所需权限的编码
     */
    String[] value() default {};

    /**
     * 权限逻辑类型
     */
    Logical logical() default Logical.AND;
}