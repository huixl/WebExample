package com.lixh.login.strategy.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lixh.login.data.entity.AccountPo;
import com.lixh.login.data.entity.LoginPo;
import com.lixh.login.data.entity.LoginTypePo;
import com.lixh.login.data.mapper.AccountMapper;
import com.lixh.login.data.mapper.LoginMapper;
import com.lixh.login.data.mapper.LoginTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.lixh.login.enums.LoginTypeEnum;
import com.lixh.login.exception.BusinessException;
import com.lixh.login.service.LoginHistoryService;
import com.lixh.login.service.TokenService;
import com.lixh.login.strategy.AbstractLoginStrategy;
import com.lixh.login.web.dto.LoginRequest;
import com.lixh.login.web.dto.LoginResponse;
import com.lixh.login.web.dto.RegisterRequest;
import com.lixh.login.web.dto.RegisterResponse;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 密码登录策略
 * @author lixionghui
 */
@Slf4j
@Component
public class PasswordStrategy extends AbstractLoginStrategy {

    private static final String USER_SESSION_KEY = "current_user";

    @Resource
    private LoginMapper loginMapper;
    @Resource
    private LoginTypeMapper loginTypeMapper;
    @Resource
    private AccountMapper accountMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    private Long currentUserId;

    public PasswordStrategy(TokenService tokenService, LoginHistoryService loginHistoryService,
        LoginTypeMapper loginTypeMapper, LoginMapper loginMapper, AccountMapper accountMapper) {
        super(tokenService, loginHistoryService, loginTypeMapper, loginMapper, accountMapper);
    }

    @Override
    public String getLoginType() {
        return LoginTypeEnum.USERNAME_PASSWORD.getCode();
    }

    @Override
    protected LoginResponse doLogin(LoginRequest request, AccountPo po) {

        // 验证密码
        if (!passwordEncoder.matches(request.getCredential(), po.getLoginPo().getCredential())) {
            throw new BusinessException("密码错误");
        }

        // 生成token
        String token = tokenService.createToken(po.getId(),request.getRememberMe());

        // 更新session
        currentUserId = po.getId();
        HttpSession session = getRequest().getSession(true);
        session.setAttribute(USER_SESSION_KEY, po);

        // 构建响应
        return LoginResponse.builder()
                .userId(po.getId())
                .username(po.getRealName())
                .nickname(po.getNickName())
                .avatarUrl(po.getAvatarUrl())
                .token(token)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected RegisterResponse doRegister(RegisterRequest request) {

        // 获取登陆类型列表
        List<LoginTypePo> loginTypeList = loginTypeMapper.selectList(new LambdaQueryWrapper<>());
        if (loginTypeList.isEmpty()) {
            throw new BusinessException("请先创建登陆方式");
        }
        Map<String, Long> collect = loginTypeList.stream().collect(Collectors.toMap(LoginTypePo::getTypeCode, LoginTypePo::getId));

        // 创建新账户
        AccountPo accountPo = new AccountPo();
        accountPo.setNickName(request.getUsername());
        accountPo.setAccountStatus(1);
        accountPo.setAvatarUrl("");
        accountMapper.insert(accountPo);

        // 账密登陆方式
        LoginPo passwordLoginPo = new LoginPo();
        passwordLoginPo.setAccountId(accountPo.getId());
        passwordLoginPo.setLoginTypeId(collect.get(LoginTypeEnum.USERNAME_PASSWORD.getCode()));
        passwordLoginPo.setIdentity(request.getUsername());
        passwordLoginPo.setCredential(passwordEncoder.encode(request.getPassword()));
        loginMapper.insert(passwordLoginPo);

        // 邮箱登陆方式
        LoginPo emailLoginPo = new LoginPo();
        emailLoginPo.setAccountId(accountPo.getId());
        emailLoginPo.setLoginTypeId(collect.get(LoginTypeEnum.USERNAME_PASSWORD.getCode()));
        emailLoginPo.setIdentity(request.getUsername());
        emailLoginPo.setCredential(passwordEncoder.encode(request.getPassword()));
        loginMapper.insert(emailLoginPo);

        // 手机登陆方式
        LoginPo phoneLoginPo = new LoginPo();
        phoneLoginPo.setAccountId(accountPo.getId());
        phoneLoginPo.setLoginTypeId(collect.get(LoginTypeEnum.USERNAME_PASSWORD.getCode()));
        phoneLoginPo.setIdentity(request.getUsername());
        phoneLoginPo.setCredential(passwordEncoder.encode(request.getPassword()));
        loginMapper.insert(phoneLoginPo);

        // 3. 构建响应
        return RegisterResponse.builder().build();
    }

    @Override
    protected void doLogout() {
        // 1. 清除session
        HttpSession session = getRequest().getSession(false);
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
            session.invalidate();
        }

        // 2. 清除当前用户ID
        currentUserId = null;
    }

    @Override
    protected Long getCurrentUserId() {
        return currentUserId;
    }
} 