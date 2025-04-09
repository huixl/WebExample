package com.lixh.login.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.login.data.entity.LoginHistoryPo;
import com.lixh.login.data.entity.LoginPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录历史Mapper接口
 * @author lixionghui
 */
@Mapper
public interface LoginMapper extends BaseMapper<LoginPo> {
}