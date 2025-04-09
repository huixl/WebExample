package com.lixh.login.service;

import com.lixh.login.data.entity.PermissionPo;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取所有权限
     *
     * @return 权限列表
     */
    List<PermissionPo> getAllPermissions();

    /**
     * 根据用户ID获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<PermissionPo> getPermissionsByUserId(Long userId);

    /**
     * 根据用户ID获取权限代码集合
     *
     * @param userId 用户ID
     * @return 权限代码集合
     */
    Set<String> getPermissionCodesByUserId(Long userId);

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionPo> getPermissionsByRoleId(Long roleId);

    /**
     * 验证用户是否拥有指定权限
     *
     * @param userId         用户ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 验证当前用户是否拥有指定权限
     *
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    boolean hasPermission(String permissionCode);
}