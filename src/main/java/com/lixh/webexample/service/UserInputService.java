package com.lixh.webexample.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lixh.webexample.data.entity.UserInputPo;

import java.util.List;

/**
 * 用户输入Service接口
 */
public interface UserInputService extends IService<UserInputPo> {

    /**
     * 获取用户输入列表
     *
     * @param parseHistoryId 解析历史ID
     * @return 用户输入列表
     */
    List<UserInputPo> getLatestUserInputs(Long parseHistoryId);

    /**
     * 添加用户输入
     *
     * @param userInput 用户输入
     * @return 添加后的用户输入
     */
    UserInputPo addUserInput(UserInputPo userInput);

    /**
     * 删除用户输入
     *
     * @param parseHistoryId 解析历史ID
     * @return 是否删除成功
     */
    boolean deleteUserInputs(Long parseHistoryId);
}