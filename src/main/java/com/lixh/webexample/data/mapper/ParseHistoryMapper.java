package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.ParseHistoryPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 解析历史Mapper接口
 */
@Mapper
public interface ParseHistoryMapper extends BaseMapper<ParseHistoryPo> {
    // 继承BaseMapper后，基本的CRUD操作已经由MyBatis-Plus提供
    // 不需要再手动编写基础SQL语句
}