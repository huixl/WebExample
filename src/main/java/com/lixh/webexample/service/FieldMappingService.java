package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.FieldMappingPo;

import java.util.List;

/**
 * 字段映射服务接口
 */
public interface FieldMappingService {

    /**
     * 获取字段映射列表
     *
     * @param parseHistoryId 解析历史ID
     * @return 字段映射列表
     */
    List<FieldMappingPo> getFieldMappings(Long parseHistoryId);

    /**
     * 根据元数据ID获取字段映射列表
     *
     * @param metadataId 元数据ID
     * @return 字段映射列表
     */
    List<FieldMappingPo> getFieldMappingsByMetadataId(Long metadataId);

    /**
     * 添加字段映射
     *
     * @param fieldMapping 字段映射
     * @return 添加后的字段映射
     */
    FieldMappingPo addFieldMapping(FieldMappingPo fieldMapping);

    /**
     * 批量添加字段映射
     *
     * @param fieldMappings 字段映射列表
     */
    void batchAddFieldMappings(List<FieldMappingPo> fieldMappings);

    /**
     * 更新字段映射
     *
     * @param fieldMapping 字段映射
     * @return 更新后的字段映射
     */
    FieldMappingPo updateFieldMapping(FieldMappingPo fieldMapping);

    /**
     * 删除字段映射
     *
     * @param parseHistoryId 解析历史ID
     * @return 是否删除成功
     */
    boolean deleteFieldMappings(Long parseHistoryId);

    /**
     * 根据解析历史ID获取字段映射
     *
     * @param parseHistoryId 解析历史ID
     * @return 字段映射列表
     */
    List<FieldMappingPo> getByParseHistoryId(Long parseHistoryId);
}