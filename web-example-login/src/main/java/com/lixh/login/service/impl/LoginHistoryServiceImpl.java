package com.lixh.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.login.data.entity.LoginHistoryPo;
import com.lixh.login.data.mapper.LoginHistoryMapper;
import com.lixh.login.service.LoginHistoryService;
import com.lixh.login.web.dto.LoginHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录历史服务实现类
 */
@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl extends ServiceImpl<LoginHistoryMapper, LoginHistoryPo>
                implements LoginHistoryService {

        @Override
        public LoginHistoryPo recordLoginHistory(
                        Long userId,
                        String loginIp,
                        String deviceInfo,
                        Integer loginStatus,
                        String loginType,
                        String remark) {

                LoginHistoryPo loginHistory = LoginHistoryPo.builder()
                                .userId(userId)
                                .loginTime(LocalDateTime.now())
                                .loginIp(loginIp)
                                .deviceInfo(deviceInfo)
                                .loginStatus(loginStatus)
                                .loginType(loginType)
                                .remark(remark)
                                .build();

                this.save(loginHistory);
                return loginHistory;
        }

        @Override
        public List<LoginHistoryResponse> getUserLoginHistory(Long userId, Integer limit) {
                if (limit == null || limit <= 0) {
                        limit = 10; // 默认显示10条
                }

                LambdaQueryWrapper<LoginHistoryPo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(LoginHistoryPo::getUserId, userId)
                                .orderByDesc(LoginHistoryPo::getLoginTime)
                                .last("LIMIT " + limit);

                List<LoginHistoryPo> list = this.list(queryWrapper);

                // 转换为DTO
                return list.stream()
                                .map(po -> LoginHistoryResponse.builder()
                                                .id(po.getId())
                                                .loginTime(po.getLoginTime())
                                                .loginIp(po.getLoginIp())
                                                .deviceInfo(po.getDeviceInfo())
                                                .loginStatus(po.getLoginStatus())
                                                .loginType(po.getLoginType())
                                                .remark(po.getRemark())
                                                .build())
                                .collect(Collectors.toList());
        }
}