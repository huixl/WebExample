package com.lixh.login.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.login.data.entity.AccountPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录历史Mapper接口
 * @author lixionghui
 */
@Mapper
public interface AccountMapper extends BaseMapper<AccountPo> {
}