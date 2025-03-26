package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.data.entity.UserInputPo;
import com.lixh.webexample.data.mapper.UserInputMapper;
import com.lixh.webexample.service.UserInputService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户输入Service实现类
 */
@Service
public class UserInputServiceImpl extends ServiceImpl<UserInputMapper, UserInputPo> implements UserInputService {

    @Override
    public List<UserInputPo> getLatestUserInputs(Long parseHistoryId) {
        LambdaQueryWrapper<UserInputPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInputPo::getParseHistoryId, parseHistoryId);
        queryWrapper.orderByDesc(UserInputPo::getCreateTime);
        queryWrapper.last("LIMIT 1");
        return this.list(queryWrapper);
    }

    @Override
    public UserInputPo addUserInput(UserInputPo userInput) {
        this.save(userInput);
        return userInput;
    }

    @Override
    public boolean deleteUserInputs(Long parseHistoryId) {
        LambdaUpdateWrapper<UserInputPo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInputPo::getParseHistoryId, parseHistoryId);
        return this.remove(updateWrapper);
    }
}