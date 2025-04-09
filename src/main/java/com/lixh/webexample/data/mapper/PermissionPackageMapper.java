package com.lixh.login.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.login.data.entity.PermissionPackagePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限包数据访问接口
 */
@Mapper
public interface PermissionPackageMapper extends BaseMapper<PermissionPackagePo> {
}