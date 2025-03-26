package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.PermissionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限数据访问接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionPo> {

    /**
     * 根据用户ID查询其所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM t_perm_permission p " +
            "JOIN t_perm_permission_package pp ON p.package_id = pp.id " +
            "JOIN t_perm_role_permission_package rpp ON pp.id = rpp.package_id " +
            "JOIN t_perm_role r ON rpp.role_id = r.id " +
            "JOIN t_perm_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "AND p.status = 1 AND pp.status = 1 AND r.status = 1 " +
            "AND p.deleted = 0 AND pp.deleted = 0 AND r.deleted = 0")
    List<PermissionPo> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询其所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM t_perm_permission p " +
            "JOIN t_perm_permission_package pp ON p.package_id = pp.id " +
            "JOIN t_perm_role_permission_package rpp ON pp.id = rpp.package_id " +
            "WHERE rpp.role_id = #{roleId} " +
            "AND p.status = 1 AND pp.status = 1 " +
            "AND p.deleted = 0 AND pp.deleted = 0")
    List<PermissionPo> selectByRoleId(@Param("roleId") Long roleId);
}