package com.lixh.login.config;

import com.lixh.login.data.entity.AccountPo;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文，用于在线程中存储当前用户信息
 */
@Slf4j
public class UserContext {

    private static final ThreadLocal<AccountPo> userThreadLocal = new ThreadLocal<>();

    /**
     * 获取当前线程的用户信息
     *
     * @return 用户实体，如果未设置则返回null
     */
    public static AccountPo getCurrentUser() {
        return userThreadLocal.get();
    }

    /**
     * 设置当前线程的用户信息
     *
     * @param user 用户实体
     */
    public static void setCurrentUser(AccountPo user) {
        userThreadLocal.set(user);
        log.debug("设置当前用户: {}", user != null ? user.getNickName() : "null");
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未设置则返回null
     */
    public static Long getCurrentUserId() {
        AccountPo user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，如果未设置则返回"system"
     */
    public static String getCurrentUsername() {
        AccountPo user = getCurrentUser();
        return user != null ? user.getNickName() : "system";
    }

    /**
     * 清除当前线程的用户信息
     */
    public static void clear() {
        userThreadLocal.remove();
        log.debug("清除当前用户上下文");
    }
}