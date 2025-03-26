package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.UserRolePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联数据访问接口
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePo> {
    // 使用MyBatis-Plus的BaseMapper接口方法，不需要手写SQL
}