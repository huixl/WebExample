package com.lixh.login.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.login.data.entity.RolePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<RolePo> {
}