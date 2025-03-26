package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.RolePo;
import com.lixh.webexample.web.dto.RoleCreateRequest;
import com.lixh.webexample.web.dto.RoleUpdateRequest;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    List<RolePo> getAllRoles();

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色实体
     */
    RolePo getRoleById(Long id);

    /**
     * 根据角色编码获取角色
     *
     * @param code 角色编码
     * @return 角色实体
     */
    RolePo getRoleByCode(String code);

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 创建后的角色
     */
    RolePo createRole(RoleCreateRequest request);

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param request 更新请求
     * @return 更新后的角色
     */
    RolePo updateRole(Long id, RoleUpdateRequest request);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 是否删除成功
     */
    boolean deleteRole(Long id);

    /**
     * 根据用户ID获取其所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RolePo> getRolesByUserId(Long userId);

    /**
     * 为用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);
}