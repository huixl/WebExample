package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.PermissionPackagePo;
import com.lixh.webexample.web.dto.PermissionPackageCreateRequest;
import com.lixh.webexample.web.dto.PermissionPackageUpdateRequest;

import java.util.List;

/**
 * 权限包服务接口
 */
public interface PermissionPackageService {

    /**
     * 获取所有权限包
     *
     * @return 权限包列表
     */
    List<PermissionPackagePo> getAllPackages();

    /**
     * 根据ID获取权限包
     *
     * @param id 权限包ID
     * @return 权限包
     */
    PermissionPackagePo getPackageById(Long id);

    /**
     * 创建权限包
     *
     * @param request 创建请求
     * @return 创建的权限包
     */
    PermissionPackagePo createPackage(PermissionPackageCreateRequest request);

    /**
     * 更新权限包
     *
     * @param id      权限包ID
     * @param request 更新请求
     * @return 更新后的权限包
     */
    PermissionPackagePo updatePackage(Long id, PermissionPackageUpdateRequest request);

    /**
     * 删除权限包
     *
     * @param id 权限包ID
     */
    void deletePackage(Long id);
}