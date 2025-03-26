package com.lixh.webexample.web.controller;

import com.lixh.webexample.config.permission.PermissionCheck;
import com.lixh.webexample.data.entity.PermissionPackagePo;
import com.lixh.webexample.data.entity.PermissionPo;
import com.lixh.webexample.data.entity.RolePo;
import com.lixh.webexample.service.PermissionPackageService;
import com.lixh.webexample.service.PermissionService;
import com.lixh.webexample.service.RoleService;
import com.lixh.webexample.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限控制器
 */
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionPackageService permissionPackageService;
    private final RoleService roleService;

    /**
     * 获取所有权限
     *
     * @return 权限列表
     */
    @GetMapping("/permissions")
    @PermissionCheck("PERMISSION:VIEW")
    public ResponseEntity<List<PermissionPo>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    /**
     * 根据用户ID获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @GetMapping("/users/{userId}/permissions")
    @PermissionCheck("PERMISSION:VIEW")
    public ResponseEntity<List<PermissionPo>> getPermissionsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.getPermissionsByUserId(userId));
    }

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/roles/{roleId}/permissions")
    @PermissionCheck("PERMISSION:VIEW")
    public ResponseEntity<List<PermissionPo>> getPermissionsByRoleId(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.getPermissionsByRoleId(roleId));
    }

    /**
     * 获取所有权限包
     *
     * @return 权限包列表
     */
    @GetMapping("/packages")
    @PermissionCheck("PERMISSION_PACKAGE:VIEW")
    public ResponseEntity<List<PermissionPackagePo>> getAllPackages() {
        return ResponseEntity.ok(permissionPackageService.getAllPackages());
    }

    /**
     * 根据ID获取权限包
     *
     * @param id 权限包ID
     * @return 权限包信息
     */
    @GetMapping("/packages/{id}")
    @PermissionCheck("PERMISSION_PACKAGE:VIEW")
    public ResponseEntity<PermissionPackagePo> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionPackageService.getPackageById(id));
    }

    /**
     * 创建权限包
     *
     * @param request 权限包创建请求
     * @return 创建的权限包
     */
    @PostMapping("/packages")
    @PermissionCheck("PERMISSION_PACKAGE:CREATE")
    public ResponseEntity<PermissionPackagePo> createPackage(
            @Valid @RequestBody PermissionPackageCreateRequest request) {
        return ResponseEntity.ok(permissionPackageService.createPackage(request));
    }

    /**
     * 更新权限包
     *
     * @param id      权限包ID
     * @param request 权限包更新请求
     * @return 更新后的权限包
     */
    @PutMapping("/packages/{id}")
    @PermissionCheck("PERMISSION_PACKAGE:UPDATE")
    public ResponseEntity<PermissionPackagePo> updatePackage(@PathVariable Long id,
            @Valid @RequestBody PermissionPackageUpdateRequest request) {
        return ResponseEntity.ok(permissionPackageService.updatePackage(id, request));
    }

    /**
     * 删除权限包
     *
     * @param id 权限包ID
     * @return 无内容响应
     */
    @DeleteMapping("/packages/{id}")
    @PermissionCheck("PERMISSION_PACKAGE:DELETE")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        permissionPackageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取用户的角色列表
     *
     * @param request 用户ID请求
     * @return 角色列表
     */
    @GetMapping("/users/roles/list")
    @PermissionCheck("USER_ROLE:VIEW")
    public List<RoleResponse> getUserRoles(@Valid UserIdRequest request) {
        List<RolePo> roles = roleService.getRolesByUserId(request.getId());
        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * 为用户分配角色
     *
     * @param request 用户角色分配请求
     * @return 是否成功
     */
    @PostMapping("/users/roles/assign")
    @PermissionCheck("USER_ROLE:ASSIGN")
    public Boolean assignRolesToUser(@Valid @RequestBody UserRoleAssignRequest request) {
        roleService.assignRolesToUser(request.getUserId(), request.getRoleIds());
        return true;
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