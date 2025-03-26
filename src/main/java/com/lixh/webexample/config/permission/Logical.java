package com.lixh.webexample.config.permission;

/**
 * 权限逻辑类型
 */
public enum Logical {
    /**
     * 与逻辑，所有权限都需要满足
     */
    AND,

    /**
     * 或逻辑，满足任意一个权限即可
     */
    OR
}