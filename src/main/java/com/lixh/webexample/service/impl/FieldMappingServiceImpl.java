package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.data.entity.FieldMappingPo;
import com.lixh.webexample.data.mapper.FieldMappingMapper;
import com.lixh.webexample.service.FieldMappingService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字段映射Service实现类
 */
@Service
public class FieldMappingServiceImpl extends ServiceImpl<FieldMappingMapper, FieldMappingPo>
        implements FieldMappingService {

    @Override
    public List<FieldMappingPo> getFieldMappings(Long parseHistoryId) {
        LambdaQueryWrapper<FieldMappingPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FieldMappingPo::getParseHistoryId, parseHistoryId);
        return this.list(queryWrapper);
    }

    @Override
    public List<FieldMappingPo> getFieldMappingsByMetadataId(Long metadataId) {
        LambdaQueryWrapper<FieldMappingPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FieldMappingPo::getMetadataId, metadataId);
        return this.list(queryWrapper);
    }

    @Override
    public FieldMappingPo addFieldMapping(FieldMappingPo fieldMapping) {
        this.save(fieldMapping);
        return fieldMapping;
    }

    @Override
    public void batchAddFieldMappings(List<FieldMappingPo> fieldMappings) {
        if (fieldMappings == null || fieldMappings.isEmpty()) {
            return;
        }
        this.saveBatch(fieldMappings);
    }

    @Override
    public FieldMappingPo updateFieldMapping(FieldMappingPo fieldMapping) {
        this.updateById(fieldMapping);
        return fieldMapping;
    }

    @Override
    public boolean deleteFieldMappings(Long parseHistoryId) {
        LambdaQueryWrapper<FieldMappingPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FieldMappingPo::getParseHistoryId, parseHistoryId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<FieldMappingPo> getByParseHistoryId(Long parseHistoryId) {
        return getFieldMappings(parseHistoryId);
    }
}