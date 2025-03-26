package com.lixh.webexample.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUsername();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, currentUser);
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUser);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);

        log.debug("自动填充插入字段 - 创建人: {}, 更新人: {}", currentUser, currentUser);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String currentUser = getCurrentUsername();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentUser);

        log.debug("自动填充更新字段 - 更新人: {}", currentUser);
    }

    /**
     * 获取当前用户名
     * 优先从UserContext中获取，如果未设置则返回"system"
     *
     * @return 当前用户名
     */
    private String getCurrentUsername() {
        return Optional.ofNullable(UserContext.getCurrentUsername())
                .orElseThrow(() -> new IllegalStateException("当前用户态没有设置"));
    }
}