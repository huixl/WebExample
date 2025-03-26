package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.RolePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<RolePo> {
}