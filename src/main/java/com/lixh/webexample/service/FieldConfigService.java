package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.FieldConfigPo;

import java.util.List;

/**
 * 字段配置Service接口
 */
public interface FieldConfigService {

    /**
     * 获取字段配置列表
     *
     * @param materialType 物料类型
     * @return 字段配置列表
     */
    List<FieldConfigPo> getFieldConfigs(String materialType);

    /**
     * 根据ID获取字段配置
     *
     * @param id 字段配置ID
     * @return 字段配置
     */
    FieldConfigPo getFieldConfigById(Long id);

    /**
     * 添加字段配置
     *
     * @param fieldConfig 字段配置
     * @return 添加后的字段配置
     */
    FieldConfigPo addFieldConfig(FieldConfigPo fieldConfig);

    /**
     * 更新字段配置
     *
     * @param fieldConfig 字段配置
     * @return 更新后的字段配置
     */
    FieldConfigPo updateFieldConfig(FieldConfigPo fieldConfig);

    /**
     * 删除字段配置
     *
     * @param id 字段配置ID
     * @return 是否删除成功
     */
    boolean deleteFieldConfig(Long id);
}