package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.RolePermissionPackagePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-权限包关联数据访问接口
 */
@Mapper
public interface RolePermissionPackageMapper extends BaseMapper<RolePermissionPackagePo> {

    /**
     * 根据角色ID查询其所有权限包ID
     *
     * @param roleId 角色ID
     * @return 权限包ID列表
     */
    @Select("SELECT package_id FROM t_role_permission_package WHERE role_id = #{roleId} AND deleted = 0")
    List<Long> selectPackageIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限包ID查询拥有该权限包的所有角色ID
     *
     * @param packageId 权限包ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM t_role_permission_package WHERE package_id = #{packageId} AND deleted = 0")
    List<Long> selectRoleIdsByPackageId(@Param("packageId") Long packageId);
}