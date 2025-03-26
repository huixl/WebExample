package com.lixh.webexample.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.constant.ParseStatus;
import com.lixh.webexample.data.entity.CalculationMappingPo;
import com.lixh.webexample.data.entity.FieldConfigPo;
import com.lixh.webexample.data.entity.ParseDetailPo;
import com.lixh.webexample.data.entity.ParseHistoryPo;
import com.lixh.webexample.data.enums.FieldType;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import com.lixh.webexample.data.mapper.CalculationMappingMapper;
import com.lixh.webexample.data.mapper.FieldConfigMapper;
import com.lixh.webexample.data.mapper.ParseDetailMapper;
import com.lixh.webexample.data.mapper.ParseHistoryMapper;
import com.lixh.webexample.service.CalculationMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 计算映射服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CalculationMappingServiceImpl extends ServiceImpl<CalculationMappingMapper, CalculationMappingPo> implements CalculationMappingService {

    private final ParseHistoryMapper parseHistoryMapper;
    private final ParseDetailMapper parseDetailMapper;
    private final FieldConfigMapper fieldConfigMapper;

    @Override
    public String generateMappingKey(String materialType, Map<String, String> conditions) {
        // 对条件进行排序，确保相同条件生成相同的key
        TreeMap<String, String> sortedConditions = new TreeMap<>(conditions);
        
        // 将材料类型和条件组合生成字符串
        StringBuilder sb = new StringBuilder();
        sb.append(materialType).append(":");
        
        for (Map.Entry<String, String> entry : sortedConditions.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        
        // 使用UUID.nameUUIDFromBytes生成确定性UUID
        return UUID.nameUUIDFromBytes(sb.toString().getBytes()).toString();
    }

    @Override
    public CalculationMappingPo saveMapping(String mappingKey, String materialType, Map<String, String> conditions, Map<String, String> results) {
        // 检查是否已存在相同mappingKey的记录
        LambdaQueryWrapper<CalculationMappingPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalculationMappingPo::getMappingKey, mappingKey);
        
        CalculationMappingPo mappingPo = this.getOne(queryWrapper);
        
        if (mappingPo == null) {
            // 创建新记录
            mappingPo = new CalculationMappingPo();
            mappingPo.setMappingKey(mappingKey);
            mappingPo.setMaterialType(materialType);
            mappingPo.setConditionJson(JSON.toJSONString(conditions));
            mappingPo.setResultJson(JSON.toJSONString(results));
            
            this.save(mappingPo);
        } else {
            // 更新已有记录
            mappingPo.setResultJson(JSON.toJSONString(results));
            this.updateById(mappingPo);
        }
        
        return mappingPo;
    }

    @Override
    public Map<String, String> getCalculationResults(String mappingKey) {
        LambdaQueryWrapper<CalculationMappingPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalculationMappingPo::getMappingKey, mappingKey);
        
        CalculationMappingPo mappingPo = this.getOne(queryWrapper);
        
        if (mappingPo != null && mappingPo.getResultJson() != null) {
            return JSON.parseObject(mappingPo.getResultJson(), Map.class);
        }
        
        return Collections.emptyMap();
    }

    /**
     * 在确认计算结果时保存AI推算字段到计算列的映射
     *
     * @param parseHistoryId 解析历史ID
     * @return 是否成功存储映射
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAiToCalculationMapping(Long parseHistoryId) {
        // 1. 查询解析历史
        ParseHistoryPo historyPo = parseHistoryMapper.selectById(parseHistoryId);
        if (historyPo == null) {
            log.error("解析历史不存在，ID: {}", parseHistoryId);
            return false;
        }

        try {
            // 2. 查询所有字段配置
            List<FieldConfigPo> allFields = fieldConfigMapper.selectList(null);
            
            // 3. 筛选出AI推算字段和计算列字段
            List<FieldConfigPo> aiInferenceFields = allFields.stream()
                    .filter(field -> FieldType.AI_INFERENCE.getValue().equals(field.getType()))
                    .collect(Collectors.toList());
            
            List<FieldConfigPo> calculatedFields = allFields.stream()
                    .filter(field -> FieldType.CALCULATED.getValue().equals(field.getType()))
                    .collect(Collectors.toList());
            
            if (aiInferenceFields.isEmpty() || calculatedFields.isEmpty()) {
                log.info("没有AI推算字段或计算列字段，无需保存映射");
                return true;
            }
            
            // 4. 查询所有解析详情记录
            LambdaQueryWrapper<ParseDetailPo> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(ParseDetailPo::getParseHistoryId, parseHistoryId);
            List<ParseDetailPo> allDetails = parseDetailMapper.selectList(detailWrapper);
            
            // 5. 按rowNum分组
            Map<Integer, List<ParseDetailPo>> detailsByRow = allDetails.stream()
                    .collect(Collectors.groupingBy(ParseDetailPo::getRowNum));
            
            // 6. 对每一行进行映射处理
            for (Map.Entry<Integer, List<ParseDetailPo>> entry : detailsByRow.entrySet()) {
                Integer rowNum = entry.getKey();
                List<ParseDetailPo> rowDetails = entry.getValue();
                
                // 提取AI推算字段的值作为条件（使用字段ID而不是字段名）
                Map<String, String> conditions = new HashMap<>();
                for (ParseDetailPo detail : rowDetails) {
                    for (FieldConfigPo aiField : aiInferenceFields) {
                        if (Objects.equals(detail.getFieldConfigId(), aiField.getId())) {
                            // 使用字段ID作为key
                            conditions.put(aiField.getId().toString(), detail.getFieldValue());
                            break;
                        }
                    }
                }
                
                // 如果没有有效的AI推算字段值，跳过此行
                if (conditions.isEmpty()) {
                    continue;
                }
                
                // 提取计算列字段的值作为结果（使用字段ID而不是字段名）
                Map<String, String> results = new HashMap<>();
                for (ParseDetailPo detail : rowDetails) {
                    for (FieldConfigPo calcField : calculatedFields) {
                        if (Objects.equals(detail.getFieldConfigId(), calcField.getId())) {
                            // 使用字段ID作为key
                            results.put(calcField.getId().toString(), detail.getFieldValue());
                            break;
                        }
                    }
                }
                
                // 生成映射键并保存映射
                String mappingKey = generateMappingKey(historyPo.getMaterialType(), conditions);
                saveMapping(mappingKey, historyPo.getMaterialType(), conditions, results);
                
                log.info("已保存行{}的AI推算字段到计算列映射，mappingKey: {}", rowNum, mappingKey);
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存AI推算字段到计算列映射时发生错误", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processCalculation(Long parseHistoryId) {
        // 1. 查询解析历史
        ParseHistoryPo historyPo = parseHistoryMapper.selectById(parseHistoryId);
        if (historyPo == null) {
            log.error("解析历史不存在，ID: {}", parseHistoryId);
            return false;
        }
        
        // 2. 更新解析历史状态为计算中
        historyPo.setParseStatus(ParseStatus.CALCULATING);
        parseHistoryMapper.updateById(historyPo);
        
        try {
            // 3. 查询所有计算列字段配置
            LambdaQueryWrapper<FieldConfigPo> fieldConfigWrapper = new LambdaQueryWrapper<>();
            fieldConfigWrapper.eq(FieldConfigPo::getType, FieldType.CALCULATED);
            List<FieldConfigPo> calculatedFields = fieldConfigMapper.selectList(fieldConfigWrapper);
            
            if (calculatedFields.isEmpty()) {
                // 如果没有计算列，直接更新状态为等待确认
                historyPo.setParseStatus(ParseStatus.WAITING_FOR_CALCULATION_CONFIRM);
                parseHistoryMapper.updateById(historyPo);
                return true;
            }
            
            // 4. 查询所有解析详情记录，按rowNum分组
            LambdaQueryWrapper<ParseDetailPo> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(ParseDetailPo::getParseHistoryId, parseHistoryId);
            List<ParseDetailPo> allDetails = parseDetailMapper.selectList(detailWrapper);
            
            // 5. 按rowNum分组
            Map<Integer, List<ParseDetailPo>> detailsByRow = allDetails.stream()
                    .collect(Collectors.groupingBy(ParseDetailPo::getRowNum));
            
            // 6. 对每一行进行计算
            for (Map.Entry<Integer, List<ParseDetailPo>> entry : detailsByRow.entrySet()) {
                Integer rowNum = entry.getKey();
                List<ParseDetailPo> rowDetails = entry.getValue();
                
                // 处理每个计算列
                for (FieldConfigPo calculatedField : calculatedFields) {
                    processCalculatedField(historyPo.getMaterialType(), rowNum, rowDetails, calculatedField);
                }
            }
            
            // 7. 更新解析历史状态为等待确认
            historyPo.setParseStatus(ParseStatus.WAITING_FOR_CALCULATION_CONFIRM);
            parseHistoryMapper.updateById(historyPo);
            
            return true;
        } catch (Exception e) {
            log.error("计算过程发生错误", e);
            // 发生异常时，更新状态为失败
            historyPo.setParseStatus(ParseStatus.FAILED);
            historyPo.setErrorMessage("计算过程发生错误: " + e.getMessage());
            parseHistoryMapper.updateById(historyPo);
            
            throw e;
        }
    }
    
    /**
     * 处理单个计算列
     */
    private void processCalculatedField(String materialType, Integer rowNum, List<ParseDetailPo> rowDetails, FieldConfigPo calculatedField) {
        // 1. 构建计算条件
        Map<String, String> conditions = new HashMap<>();
        
        for (ParseDetailPo detail : rowDetails) {
            // 跳过计算列自身
            if (Objects.equals(detail.getFieldConfigId(), calculatedField.getId())) {
                continue;
            }
            
            // 直接使用字段ID作为键
            conditions.put(detail.getFieldConfigId().toString(), detail.getFieldValue());
        }
        
        // 2. 生成映射键
        String mappingKey = generateMappingKey(materialType, conditions);
        
        // 3. 查询计算结果
        Map<String, String> results = getCalculationResults(mappingKey);
        
        // 4. 如果没有计算结果，暂时设为空
        String calculatedValue = results.getOrDefault(calculatedField.getId().toString(), "");
        
        // 5. 查找或创建计算列的ParseDetail记录
        ParseDetailPo calculatedDetail = null;
        for (ParseDetailPo detail : rowDetails) {
            if (Objects.equals(detail.getFieldConfigId(), calculatedField.getId())) {
                calculatedDetail = detail;
                break;
            }
        }
        
        if (calculatedDetail == null) {
            // 创建新的计算列记录
            calculatedDetail = new ParseDetailPo();
            calculatedDetail.setParseHistoryId(rowDetails.get(0).getParseHistoryId());
            calculatedDetail.setRowNum(rowNum);
            calculatedDetail.setFieldConfigId(calculatedField.getId());
            calculatedDetail.setFieldName(calculatedField.getName());
            calculatedDetail.setFieldValue(calculatedValue);
            calculatedDetail.setStatus(ParseDetailStatus.PENDING);
            
            parseDetailMapper.insert(calculatedDetail);
        } else {
            // 更新已有记录
            calculatedDetail.setFieldValue(calculatedValue);
            calculatedDetail.setStatus(ParseDetailStatus.PENDING);
            
            parseDetailMapper.updateById(calculatedDetail);
        }
    }
}
