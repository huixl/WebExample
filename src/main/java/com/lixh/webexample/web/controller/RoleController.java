package com.lixh.login.web.controller;

import com.lixh.login.config.permission.PermissionCheck;
import com.lixh.login.data.entity.RolePo;
import com.lixh.login.service.RoleService;
import com.lixh.login.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/permission/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取所有角色
     *
     * @return 角色列表响应
     */
    @GetMapping("/list")
    @PermissionCheck("ROLE:VIEW")
    public RoleListResponse listRoles() {
        List<RolePo> roles = roleService.getAllRoles();
        RoleListResponse response = new RoleListResponse();
        response.setRoles(roles.stream().map(this::convertToRoleResponse).collect(Collectors.toList()));
        return response;
    }

    /**
     * 根据ID获取角色
     *
     * @param request 角色ID请求
     * @return 角色响应
     */
    @GetMapping("/get")
    @PermissionCheck("ROLE:VIEW")
    public RoleResponse getRole(@Valid RoleIdRequest request) {
        RolePo role = roleService.getRoleById(request.getId());
        return convertToRoleResponse(role);
    }

    /**
     * 创建角色
     *
     * @param request 角色创建请求
     * @return 角色响应
     */
    @PostMapping("/create")
    @PermissionCheck("ROLE:CREATE")
    public RoleResponse createRole(@Valid @RequestBody RoleCreateRequest request) {
        RolePo role = roleService.createRole(request);
        return convertToRoleResponse(role);
    }

    /**
     * 更新角色
     *
     * @param request 角色更新请求（包含ID）
     * @return 角色响应
     */
    @PostMapping("/update")
    @PermissionCheck("ROLE:UPDATE")
    public RoleResponse updateRole(@Valid @RequestBody RoleUpdateWithIdRequest request) {
        RoleUpdateRequest updateRequest = new RoleUpdateRequest();
        updateRequest.setName(request.getName());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setStatus(request.getStatus());

        RolePo role = roleService.updateRole(request.getId(), updateRequest);
        return convertToRoleResponse(role);
    }

    /**
     * 删除角色
     *
     * @param request 角色ID请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @PermissionCheck("ROLE:DELETE")
    public Boolean deleteRole(@Valid @RequestBody RoleIdRequest request) {
        return roleService.deleteRole(request.getId());
    }

    /**
     * 将RolePo转换为RoleResponse
     *
     * @param rolePo 角色实体
     * @return 角色响应
     */
    private RoleResponse convertToRoleResponse(RolePo rolePo) {
        if (rolePo == null) {
            return null;
        }
        RoleResponse response = new RoleResponse();
        response.setId(rolePo.getId());
        response.setName(rolePo.getName());
        response.setCode(rolePo.getCode());
        response.setDescription(rolePo.getDescription());
        response.setStatus(rolePo.getStatus());
        response.setCreateTime(rolePo.getCreateTime());
        response.setUpdateTime(rolePo.getUpdateTime());
        return response;
    }
}