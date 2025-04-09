package com.lixh.login.service;

import com.lixh.login.data.entity.LoginHistoryPo;
import com.lixh.login.web.dto.LoginHistoryResponse;

import java.util.List;

/**
 * 登录历史服务接口
 */
public interface LoginHistoryService {

    /**
     * 记录登录历史
     *
     * @param userId      用户ID
     * @param loginIp     登录IP
     * @param deviceInfo  设备信息
     * @param loginStatus 登录状态：0-失败，1-成功
     * @param loginType   登录类型：PASSWORD-密码登录，TOKEN-令牌登录
     * @param remark      备注
     * @return 登录历史记录
     */
    LoginHistoryPo recordLoginHistory(
            Long userId,
            String loginIp,
            String deviceInfo,
            Integer loginStatus,
            String loginType,
            String remark);

    /**
     * 获取用户的登录历史列表
     *
     * @param userId 用户ID
     * @param limit  限制数量，默认为10
     * @return 登录历史列表
     */
    List<LoginHistoryResponse> getUserLoginHistory(Long userId, Integer limit);
}