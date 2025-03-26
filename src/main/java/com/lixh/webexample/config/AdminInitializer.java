package com.lixh.webexample.config;

import com.lixh.webexample.data.entity.UserPo;
import com.lixh.webexample.service.UserService;
import com.lixh.webexample.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 管理员初始化器
 * 在应用启动时检查管理员用户是否存在，如果不存在则创建
 */
@Component
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final AdminConfig adminConfig;
    private final UserService userService;

    public AdminInitializer(AdminConfig adminConfig, UserService userService) {
        this.adminConfig = adminConfig;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        initializeAdminUser();
    }

    /**
     * 初始化管理员用户
     */
    private void initializeAdminUser() {
        String adminUsername = adminConfig.getUsername();

        // 检查管理员用户是否存在
        UserPo existingAdmin = userService.findByUsername(adminUsername);

        if (existingAdmin == null) {
            log.info("管理员用户 [{}] 不存在，开始创建...", adminUsername);

            // 创建注册请求
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(adminUsername);
            registerRequest.setPassword(adminConfig.getPassword());
            registerRequest.setConfirmPassword(adminConfig.getPassword());
            registerRequest.setEmail(adminConfig.getEmail());

            try {
                // 注册管理员用户
                userService.register(registerRequest);
                log.info("管理员用户 [{}] 创建成功", adminUsername);
            } catch (Exception e) {
                log.error("创建管理员用户 [{}] 失败: {}", adminUsername, e.getMessage(), e);
            }
        } else {
            log.info("管理员用户 [{}] 已存在，无需创建", adminUsername);
        }
    }
}