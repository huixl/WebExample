package com.lixh.webexample.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.data.entity.DatabasePo;
import com.lixh.webexample.data.mapper.DatabaseMapper;
import com.lixh.webexample.service.DatabaseService;

import lombok.extern.slf4j.Slf4j;

/**
 * 垫片数据库服务实现类
 */
@Service
@Slf4j
public class DatabaseServiceImpl extends ServiceImpl<DatabaseMapper, DatabasePo>
        implements DatabaseService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<DatabasePo> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }

        // 使用MyBatis-Plus的批量保存方法
        saveBatch(records);
        return records.size();
    }

    @Override
    public List<DatabasePo> getByBatchId(String batchId) {
        LambdaQueryWrapper<DatabasePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatabasePo::getBatchId, batchId);
        return list(queryWrapper);
    }

    @Override
    public List<DatabasePo> getByMaterialType(String materialType) {
        LambdaQueryWrapper<DatabasePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatabasePo::getMaterialType, materialType);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByBatchId(String batchId) {
        LambdaQueryWrapper<DatabasePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatabasePo::getBatchId, batchId);
        return baseMapper.delete(queryWrapper);
    }
}