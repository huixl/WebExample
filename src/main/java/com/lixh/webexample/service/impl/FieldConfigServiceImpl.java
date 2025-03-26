package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.data.entity.FieldConfigPo;
import com.lixh.webexample.data.mapper.FieldConfigMapper;
import com.lixh.webexample.service.FieldConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字段配置Service实现类
 */
@Service
public class FieldConfigServiceImpl extends ServiceImpl<FieldConfigMapper, FieldConfigPo>
        implements FieldConfigService {

    @Override
    public List<FieldConfigPo> getFieldConfigs(String materialType) {
        LambdaQueryWrapper<FieldConfigPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FieldConfigPo::getMaterialType, materialType);
        return this.list(queryWrapper);
    }

    @Override
    public FieldConfigPo getFieldConfigById(Long id) {
        return this.getById(id);
    }

    @Override
    public FieldConfigPo addFieldConfig(FieldConfigPo fieldConfig) {
        // 检查是否已存在同名同类型的字段
        LambdaQueryWrapper<FieldConfigPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FieldConfigPo::getName, fieldConfig.getName())
                .eq(FieldConfigPo::getType, fieldConfig.getType())
                .eq(FieldConfigPo::getMaterialType, fieldConfig.getMaterialType());

        FieldConfigPo existingConfig = this.getOne(queryWrapper);

        if (existingConfig != null) {
            return existingConfig;
        }

        // 保存字段配置 (createTime, updateTime, deleted等字段由MybatisPlusConfig自动填充)
        this.save(fieldConfig);

        return fieldConfig;
    }

    @Override
    public FieldConfigPo updateFieldConfig(FieldConfigPo fieldConfig) {
        // 获取原字段配置
        FieldConfigPo existingConfig = this.getById(fieldConfig.getId());

        if (existingConfig == null) {
            return null;
        }

        // 如果是"待解析"且类型为"系统"的字段，不允许修改字段名称和类型
        if ("待解析".equals(existingConfig.getName()) && "系统".equals(existingConfig.getType())) {
            fieldConfig.setName(existingConfig.getName());
            fieldConfig.setType(existingConfig.getType());
        }

        // 更新字段配置 (updateTime和updateBy由MybatisPlusConfig自动填充)
        this.updateById(fieldConfig);

        return fieldConfig;
    }

    @Override
    public boolean deleteFieldConfig(Long id) {
        // 获取字段配置
        FieldConfigPo fieldConfig = this.getById(id);

        if (fieldConfig == null) {
            return false;
        }

        // 如果是"待解析"且类型为"系统"的字段，不允许删除
        if ("待解析".equals(fieldConfig.getName()) && "系统".equals(fieldConfig.getType())) {
            return false;
        }

        // 逻辑删除字段配置 (updateTime和updateBy由MybatisPlusConfig自动填充)
        return this.removeById(id);
    }
}