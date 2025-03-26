package com.lixh.webexample.service;

import java.util.List;

import com.lixh.webexample.data.entity.DatabasePo;

/**
 * 垫片数据库服务接口
 */
public interface DatabaseService {

    /**
     * 批量保存垫片数据库记录
     *
     * @param records 记录列表
     * @return 保存的记录数量
     */
    int batchSave(List<DatabasePo> records);

    /**
     * 根据批次ID查询记录
     *
     * @param batchId 批次ID
     * @return 记录列表
     */
    List<DatabasePo> getByBatchId(String batchId);

    /**
     * 根据材料类型查询记录
     *
     * @param materialType 材料类型
     * @return 记录列表
     */
    List<DatabasePo> getByMaterialType(String materialType);

    /**
     * 根据批次ID删除记录
     *
     * @param batchId 批次ID
     * @return 删除的记录数量
     */
    int deleteByBatchId(String batchId);
}