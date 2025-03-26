package com.lixh.webexample.data.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.DatabasePo;

/**
 * 垫片数据库Mapper接口
 */
@Mapper
public interface DatabaseMapper extends BaseMapper<DatabasePo> {
}