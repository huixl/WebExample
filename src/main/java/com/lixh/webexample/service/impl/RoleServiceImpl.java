package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.config.UserContext;
import com.lixh.webexample.data.entity.RolePo;
import com.lixh.webexample.data.entity.UserRolePo;
import com.lixh.webexample.data.mapper.RoleMapper;
import com.lixh.webexample.data.mapper.UserRoleMapper;
import com.lixh.webexample.service.RoleService;
import com.lixh.webexample.web.dto.RoleCreateRequest;
import com.lixh.webexample.web.dto.RoleUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RolePo> implements RoleService {

    private final UserRoleMapper userRoleMapper;

    @Override
    public List<RolePo> getAllRoles() {
        LambdaQueryWrapper<RolePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(RolePo::getId);
        return list(queryWrapper);
    }

    @Override
    public RolePo getRoleById(Long id) {
        return getById(id);
    }

    @Override
    public RolePo getRoleByCode(String code) {
        LambdaQueryWrapper<RolePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePo::getCode, code);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RolePo createRole(RoleCreateRequest request) {
        // 检查角色编码是否已存在
        RolePo existingRole = getRoleByCode(request.getCode());
        if (existingRole != null) {
            throw new RuntimeException("角色编码已存在");
        }

        // 创建新角色
        RolePo role = new RolePo();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setCreateBy(UserContext.getCurrentUsername());
        role.setUpdateBy(UserContext.getCurrentUsername());

        // 保存角色
        save(role);
        log.info("角色创建成功: {}", role.getCode());

        return role;
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RolePo updateRole(Long id, RoleUpdateRequest request) {
        // 检查角色是否存在
        RolePo role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 超级管理员角色不允许修改
        if ("SUPER_ADMIN".equals(role.getCode())) {
            throw new RuntimeException("超级管理员角色不允许修改");
        }

        // 更新角色信息
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }
        role.setUpdateBy(UserContext.getCurrentUsername());

        // 更新角色
        updateById(role);
        log.info("角色更新成功: {}", role.getCode());

        return role;
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public boolean deleteRole(Long id) {
        // 检查角色是否存在
        RolePo role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 超级管理员和管理员角色不允许删除
        if ("SUPER_ADMIN".equals(role.getCode()) || "ADMIN".equals(role.getCode())) {
            throw new RuntimeException("系统内置角色不允许删除");
        }

        // 删除角色
        return removeById(id);
    }

    @Override
    public List<RolePo> getRolesByUserId(Long userId) {
        // 使用MyBatis-Plus的Wrapper查询用户角色关系
        LambdaQueryWrapper<UserRolePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRolePo::getUserId, userId)
                .eq(UserRolePo::getDeleted, 0);
        List<UserRolePo> userRoles = userRoleMapper.selectList(queryWrapper);

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取角色ID
        List<Long> roleIds = userRoles.stream()
                .map(UserRolePo::getRoleId)
                .collect(Collectors.toList());

        // 查询所有角色
        LambdaQueryWrapper<RolePo> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.in(RolePo::getId, roleIds)
                .eq(RolePo::getStatus, 1);
        return list(roleQueryWrapper);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles", "userRoles"}, key = "#userId")
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 使用MyBatis-Plus的Wrapper逻辑删除现有角色关系
        LambdaUpdateWrapper<UserRolePo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserRolePo::getUserId, userId)
                .set(UserRolePo::getDeleted, 1)
                .set(UserRolePo::getUpdateBy, UserContext.getCurrentUsername());
        userRoleMapper.update(null, updateWrapper);

        // 如果角色ID列表为空，则只是清空该用户的角色，不添加新角色
        if (roleIds == null || roleIds.isEmpty()) {
            log.info("清空用户角色关系: userId={}", userId);
            return;
        }

        // 为用户添加新的角色关系
        List<UserRolePo> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            UserRolePo userRole = new UserRolePo();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateBy(UserContext.getCurrentUsername());
            userRole.setUpdateBy(UserContext.getCurrentUsername());
            userRoles.add(userRole);
        }

        // 批量插入用户角色关系
        userRoles.forEach(userRoleMapper::insert);
        log.info("为用户分配角色成功: userId={}, roleIds={}", userId, roleIds);
    }
}