package com.lixh.login.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.login.data.entity.UserPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPo> {

}