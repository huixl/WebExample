package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.CalculationMappingPo;
import java.util.Map;

/**
 * 计算映射服务接口
 */
public interface CalculationMappingService {

    /**
     * 生成计算映射键
     *
     * @param materialType 材料类型
     * @param conditions 计算条件
     * @return 映射键
     */
    String generateMappingKey(String materialType, Map<String, String> conditions);

    /**
     * 保存计算映射
     *
     * @param mappingKey 映射键
     * @param materialType 材料类型
     * @param conditions 计算条件
     * @param results 计算结果
     * @return 保存的实体
     */
    CalculationMappingPo saveMapping(String mappingKey, String materialType, Map<String, String> conditions, Map<String, String> results);

    /**
     * 查询计算结果
     *
     * @param mappingKey 映射键
     * @return 计算结果
     */
    Map<String, String> getCalculationResults(String mappingKey);

    /**
     * 执行计算处理
     *
     * @param parseHistoryId 解析历史ID
     * @return 处理结果
     */
    boolean processCalculation(Long parseHistoryId);
    
    /**
     * 在确认计算结果时保存AI推算字段到计算列的映射
     *
     * @param parseHistoryId 解析历史ID
     * @return 是否成功存储映射
     */
    boolean saveAiToCalculationMapping(Long parseHistoryId);
}
