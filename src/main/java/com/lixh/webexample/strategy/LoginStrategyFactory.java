package com.lixh.webexample.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lixh.webexample.exception.BusinessException;

import jakarta.annotation.PostConstruct;

/**
 * 登录策略工厂
 * @author lixionghui
 */
@Component
public class LoginStrategyFactory {

    @Autowired
    private List<LoginStrategy> loginStrategies;

    private final Map<String, LoginStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        loginStrategies.forEach(strategy -> strategyMap.put(strategy.getLoginType(), strategy));
    }

    /**
     * 获取登录策略
     * @param loginType 登录方式
     * @return 登录策略
     */
    public LoginStrategy getStrategy(String loginType) {

        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new BusinessException("不支持的登录方式: " + loginType);
        }
        return strategy;
    }
} 