package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.config.UserContext;
import com.lixh.webexample.data.entity.PermissionPo;
import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.data.mapper.PermissionMapper;
import com.lixh.webexample.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, PermissionPo> implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionPo> getAllPermissions() {
        LambdaQueryWrapper<PermissionPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PermissionPo::getStatus, 1)
                .orderByAsc(PermissionPo::getPackageId)
                .orderByAsc(PermissionPo::getId);
        return list(queryWrapper);
    }

    @Override
    public List<PermissionPo> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return permissionMapper.selectByUserId(userId);
    }

    @Override
    public Set<String> getPermissionCodesByUserId(Long userId) {
        List<PermissionPo> permissions = getPermissionsByUserId(userId);
        return permissions.stream()
                .map(PermissionPo::getCode)
                .collect(Collectors.toSet());
    }

    @Override
    public List<PermissionPo> getPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return permissionMapper.selectByRoleId(roleId);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null || permissionCode.isEmpty()) {
            return false;
        }

        // 获取用户权限集合
        Set<String> permissionCodes = getPermissionCodesByUserId(userId);

        // 验证权限
        return permissionCodes.contains(permissionCode);
    }

    @Override
    public boolean hasPermission(String permissionCode) {
        // 获取当前用户
        UserPo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return hasPermission(currentUser.getId(), permissionCode);
    }
}