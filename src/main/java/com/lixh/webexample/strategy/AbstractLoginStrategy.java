package com.lixh.webexample.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lixh.webexample.data.entity.AccountPo;
import com.lixh.webexample.data.entity.LoginPo;
import com.lixh.webexample.data.entity.LoginTypePo;
import com.lixh.webexample.data.mapper.AccountMapper;
import com.lixh.webexample.data.mapper.LoginMapper;
import com.lixh.webexample.data.mapper.LoginTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.lixh.webexample.exception.BusinessException;
import com.lixh.webexample.service.LoginHistoryService;
import com.lixh.webexample.service.TokenService;
import com.lixh.webexample.web.dto.LoginRequest;
import com.lixh.webexample.web.dto.LoginResponse;
import com.lixh.webexample.web.dto.RegisterRequest;
import com.lixh.webexample.web.dto.RegisterResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录策略抽象基类
 * @author lixionghui
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractLoginStrategy implements LoginStrategy {

    protected final TokenService tokenService;
    protected final LoginHistoryService loginHistoryService;
    protected final LoginTypeMapper loginTypeMapper;
    protected final LoginMapper loginMapper;
    protected final AccountMapper accountMapper;


    @Override
    public LoginResponse processLogin(LoginRequest request) {

        try {

            // 验证账户是否存在
            AccountPo po = validAccountLogin(request.getIdentity(), request.getLoginType());

            // 执行具体的登录逻辑
            LoginResponse response = doLogin(request,po);

            // 记录登录历史
            //recordLoginHistory(response.getUserId(), true, "登录成功");

            // 返回
            return response;
        } catch (BusinessException e) {
            // 4. 记录登录失败历史
            recordLoginHistory(null, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 5. 记录登录失败历史
            recordLoginHistory(null, false, "登录失败：系统错误");
            throw new BusinessException("登录失败：系统错误", e);
        }
    }

    @Override
    public RegisterResponse processRegister(RegisterRequest request) {

        try {

            // 账户是否已经存在
            validAccountRegister(request.getUsername(), request.getLoginType());

            // 执行具体注册逻辑
            RegisterResponse response = doRegister(request);

            // 记录注册历史
            //recordLoginHistory(1L, true, "注册成功");

            // 返回
            return response;
        } catch (BusinessException e) {
            // 4. 记录注册失败历史
            recordLoginHistory(null, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 5. 记录注册失败历史
            recordLoginHistory(null, false, "注册失败：系统错误");
            throw new BusinessException("注册失败：系统错误", e);
        }
    }

    private AccountPo validAccountLogin(String identity,String loginType){

        LambdaQueryWrapper<LoginTypePo> loginTypeWrapper = new LambdaQueryWrapper<>();
        loginTypeWrapper.eq(LoginTypePo::getTypeCode, loginType);
        LoginTypePo typePo = loginTypeMapper.selectOne(loginTypeWrapper);

        // 验证该登陆方式是否支持
        if(typePo == null){
            throw new BusinessException("该登陆类型暂不支持");
        }


        LambdaQueryWrapper<LoginPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginPo::getIdentity, identity).eq(LoginPo::getLoginTypeId,typePo.getId());
        LoginPo loginPo = loginMapper.selectOne(queryWrapper);

        // 该登陆方式是否存在数据
        if(loginPo == null){
            throw new BusinessException("账户不存在");
        }

        LambdaQueryWrapper<AccountPo> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(AccountPo::getId, loginPo.getAccountId());
        AccountPo accountPo = accountMapper.selectOne(accountWrapper);

        // 验证账户是否被禁用
        if(accountPo == null || accountPo.getAccountStatus() != 0){
            throw new BusinessException("账户当前不可用");
        }

        accountPo.setLoginPo(loginPo);
        return accountPo;
    }

    private void validAccountRegister(String identity,String loginType){

        LambdaQueryWrapper<LoginTypePo> loginTypeWrapper = new LambdaQueryWrapper<>();
        loginTypeWrapper.eq(LoginTypePo::getTypeCode, loginType);
        LoginTypePo typePo = loginTypeMapper.selectOne(loginTypeWrapper);

        // 验证该登陆方式是否支持
        if(typePo == null){
            throw new BusinessException("该登陆类型暂不支持");
        }


        LambdaQueryWrapper<LoginPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginPo::getIdentity, identity).eq(LoginPo::getLoginTypeId,typePo.getId());
        LoginPo loginPo = loginMapper.selectOne(queryWrapper);

        // 该登陆方式是否存在数据
        if(loginPo != null){
            throw new BusinessException("账户已存在");
        }

    }

    @Override
    public void processLogout() {
        try {
            // 1. 执行具体登出逻辑
            doLogout();

            // 2. 记录登出历史
            recordLoginHistory(getCurrentUserId(), true, "登出成功");
        } catch (Exception e) {
            log.error("登出失败", e);
            throw new BusinessException("登出失败：系统错误", e);
        }
    }

    /**
     * 执行具体的登录逻辑
     */
    protected abstract LoginResponse doLogin(LoginRequest request,AccountPo po);

    /**
     * 执行具体的注册逻辑
     */
    protected abstract RegisterResponse doRegister(RegisterRequest request);

    /**
     * 执行具体的登出逻辑
     */
    protected abstract void doLogout();

    /**
     * 获取当前用户ID
     */
    protected abstract Long getCurrentUserId();

    /**
     * 记录登录历史
     */
    protected void recordLoginHistory(Long userId, boolean success, String message) {
        try {
            loginHistoryService.recordLoginHistory(
                userId,
                getClientIp(),
                getRequest().getHeader("User-Agent"),
                success ? 1 : 0,
                getLoginType(),
                message
            );
        } catch (Exception e) {
            log.error("记录登录历史失败", e);
        }
    }

    /**
     * 获取客户端IP
     */
    protected String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }

    /**
     * 获取当前请求
     */
    protected HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.error("获取当前请求失败", e);
            return null;
        }
    }
} 