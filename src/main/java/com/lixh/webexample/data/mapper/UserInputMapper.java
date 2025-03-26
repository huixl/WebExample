package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.UserInputPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户输入Mapper接口
 */
@Mapper
public interface UserInputMapper extends BaseMapper<UserInputPo> {
}