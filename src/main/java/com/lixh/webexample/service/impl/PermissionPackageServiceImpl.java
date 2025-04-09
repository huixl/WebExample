package com.lixh.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.login.data.entity.PermissionPackagePo;
import com.lixh.login.data.mapper.PermissionPackageMapper;
import com.lixh.login.service.PermissionPackageService;
import com.lixh.login.web.dto.PermissionPackageCreateRequest;
import com.lixh.login.web.dto.PermissionPackageUpdateRequest;
import com.lixh.login.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限包服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionPackageServiceImpl extends ServiceImpl<PermissionPackageMapper, PermissionPackagePo>
        implements PermissionPackageService {

    /**
     * 获取所有权限包
     *
     * @return 权限包列表
     */
    @Override
    @Cacheable(value = "permission_packages")
    public List<PermissionPackagePo> getAllPackages() {
        LambdaQueryWrapper<PermissionPackagePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PermissionPackagePo::getStatus, 1)
                .orderByAsc(PermissionPackagePo::getId);
        return list(queryWrapper);
    }

    /**
     * 根据ID获取权限包
     *
     * @param id 权限包ID
     * @return 权限包
     */
    @Override
    @Cacheable(value = "permission_package", key = "#id")
    public PermissionPackagePo getPackageById(Long id) {
        PermissionPackagePo packagePo = getById(id);
        if (packagePo == null) {
            throw new ResourceNotFoundException("权限包不存在");
        }
        return packagePo;
    }

    /**
     * 创建权限包
     *
     * @param request 创建请求
     * @return 创建的权限包
     */
    @Override
    @Transactional
    @CacheEvict(value = {"permission_packages", "permission_package"}, allEntries = true)
    public PermissionPackagePo createPackage(PermissionPackageCreateRequest request) {
        PermissionPackagePo packagePo = new PermissionPackagePo();
        packagePo.setName(request.getName());
        packagePo.setCode(request.getCode());
        packagePo.setDescription(request.getDescription());
        packagePo.setStatus(request.getStatus());

        save(packagePo);
        return packagePo;
    }

    /**
     * 更新权限包
     *
     * @param id      权限包ID
     * @param request 更新请求
     * @return 更新后的权限包
     */
    @Override
    @Transactional
    @CacheEvict(value = {"permission_packages", "permission_package"}, allEntries = true)
    public PermissionPackagePo updatePackage(Long id, PermissionPackageUpdateRequest request) {
        PermissionPackagePo packagePo = getById(id);
        if (packagePo == null) {
            throw new ResourceNotFoundException("权限包不存在");
        }

        packagePo.setName(request.getName());
        packagePo.setCode(request.getCode());
        packagePo.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            packagePo.setStatus(request.getStatus());
        }

        updateById(packagePo);
        return packagePo;
    }

    /**
     * 删除权限包
     *
     * @param id 权限包ID
     */
    @Override
    @Transactional
    @CacheEvict(value = {"permission_packages", "permission_package"}, allEntries = true)
    public void deletePackage(Long id) {
        PermissionPackagePo packagePo = getById(id);
        if (packagePo == null) {
            throw new ResourceNotFoundException("权限包不存在");
        }

        removeById(id);
    }
}