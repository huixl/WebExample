package com.lixh.webexample.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lixh.webexample.data.entity.FieldConfigPo;
import com.lixh.webexample.data.entity.DatabasePo;
import com.lixh.webexample.data.enums.RelationType;
import com.lixh.webexample.service.FieldConfigService;
import com.lixh.webexample.service.DatabaseService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 垫片预测工具类
 */
@Component
@Slf4j
public class GasketPredictionTool {

    @Autowired
    private DatabaseService gasketDatabaseService;

    @Autowired
    private FieldConfigService fieldConfigService;

    /**
     * 预测字段值
     * 
     * @param knownFieldsStr   已知字段值字符串，格式："fieldName1=已知值1;fieldName2=已知值2"
     * @param predictFieldName 需要预测的字段名称
     * @return 预测的可能值列表
     */
    @Tool("根据已知字段值预测目标字段的可能值，帮助用户填写垫片信息")
    public List<String> predictValue(
            @P("已知字段值字符串，格式：'fieldName1=已知值1;fieldName2=已知值2'") String knownFieldsStr,
            @P("需要预测的字段名称") String predictFieldName) {

        log.info("开始预测字段值，已知字段：{}，预测字段：{}", knownFieldsStr, predictFieldName);

        try {
            // 解析已知字段值字符串
            Map<String, String> knownFields = parseKnownFieldsStr(knownFieldsStr);
            if (knownFields.isEmpty()) {
                return List.of("无法解析已知字段值，请检查格式是否正确");
            }

            // 获取所有可能的材料类型
            // 这里假设我们有一个固定的材料类型列表，实际应用中可能需要从配置或数据库中获取
            List<String> materialTypes = List.of("GASKET", "BOLT", "NUT", "WASHER");

            // 获取所有字段配置
            List<FieldConfigPo> allFieldConfigs = new ArrayList<>();
            for (String materialType : materialTypes) {
                List<FieldConfigPo> configs = fieldConfigService.getFieldConfigs(materialType);
                allFieldConfigs.addAll(configs);
            }

            // 创建字段名称到ID的映射
            Map<String, Long> fieldNameToIdMap = new HashMap<>();
            for (FieldConfigPo config : allFieldConfigs) {
                fieldNameToIdMap.put(config.getName(), config.getId());
            }

            // 获取预测字段的ID
            Long predictFieldId = fieldNameToIdMap.get(predictFieldName);
            if (predictFieldId == null) {
                return List.of("未找到预测字段：" + predictFieldName);
            }

            // 查询数据库中的记录
            List<DatabasePo> allRecords = new ArrayList<>();

            // 获取所有材料类型的记录
            for (String materialType : materialTypes) {
                List<DatabasePo> records = gasketDatabaseService.getByMaterialType(materialType);
                allRecords.addAll(records);
            }

            // 根据已知字段值筛选记录
            List<DatabasePo> filteredRecords = filterRecordsByKnownFields(allRecords, knownFields, fieldNameToIdMap);

            // 从筛选后的记录中提取预测字段的可能值
            List<String> possibleValues = extractPossibleValues(filteredRecords, predictFieldId);

            if (possibleValues.isEmpty()) {
                return List.of("未找到符合条件的预测值");
            }

            return possibleValues;
        } catch (Exception e) {
            log.error("预测字段值失败", e);
            return List.of("预测失败：" + e.getMessage());
        }
    }

    /**
     * 解析已知字段值字符串
     * 
     * @param knownFieldsStr 已知字段值字符串
     * @return 字段名称到值的映射
     */
    private Map<String, String> parseKnownFieldsStr(String knownFieldsStr) {
        Map<String, String> result = new HashMap<>();

        if (knownFieldsStr == null || knownFieldsStr.trim().isEmpty()) {
            return result;
        }

        String[] fieldPairs = knownFieldsStr.split(";");
        for (String pair : fieldPairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String fieldName = parts[0].trim();
                String fieldValue = parts[1].trim();
                if (!fieldName.isEmpty() && !fieldValue.isEmpty()) {
                    result.put(fieldName, fieldValue);
                }
            }
        }

        return result;
    }

    /**
     * 根据已知字段值筛选记录
     * 
     * @param allRecords       所有记录
     * @param knownFields      已知字段值
     * @param fieldNameToIdMap 字段名称到ID的映射
     * @return 筛选后的记录
     */
    private List<DatabasePo> filterRecordsByKnownFields(
            List<DatabasePo> allRecords,
            Map<String, String> knownFields,
            Map<String, Long> fieldNameToIdMap) {

        List<DatabasePo> result = new ArrayList<>();

        for (DatabasePo record : allRecords) {
            boolean match = true;

            for (Map.Entry<String, String> entry : knownFields.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                Long fieldId = fieldNameToIdMap.get(fieldName);

                if (fieldId == null) {
                    match = false;
                    break;
                }

                // 检查记录是否匹配已知字段值
                if (RelationType.NO_RELATION.getValue().equals(record.getRelationType())) {
                    // 无关系记录
                    if (fieldId.equals(record.getParentFieldId()) &&
                            !fieldValue.equals(record.getParentValue())) {
                        match = false;
                        break;
                    }
                } else if (RelationType.PARENT_CHILD.getValue().equals(record.getRelationType())) {
                    // 父子关系记录
                    if (fieldId.equals(record.getParentFieldId()) &&
                            !fieldValue.equals(record.getParentValue())) {
                        match = false;
                        break;
                    } else if (fieldId.equals(record.getChildFieldId()) &&
                            !fieldValue.equals(record.getChildValue())) {
                        match = false;
                        break;
                    }
                }
            }

            if (match) {
                result.add(record);
            }
        }

        return result;
    }

    /**
     * 从筛选后的记录中提取预测字段的可能值
     * 
     * @param filteredRecords 筛选后的记录
     * @param predictFieldId  预测字段ID
     * @return 可能的预测值列表
     */
    private List<String> extractPossibleValues(List<DatabasePo> filteredRecords, Long predictFieldId) {
        List<String> result = new ArrayList<>();

        for (DatabasePo record : filteredRecords) {
            String value = null;

            if (RelationType.NO_RELATION.getValue().equals(record.getRelationType())) {
                // 无关系记录
                if (predictFieldId.equals(record.getParentFieldId())) {
                    value = record.getParentValue();
                }
            } else if (RelationType.PARENT_CHILD.getValue().equals(record.getRelationType())) {
                // 父子关系记录
                if (predictFieldId.equals(record.getParentFieldId())) {
                    value = record.getParentValue();
                } else if (predictFieldId.equals(record.getChildFieldId())) {
                    value = record.getChildValue();
                }
            }

            if (value != null && !value.isEmpty() && !result.contains(value)) {
                result.add(value);
            }
        }

        return result;
    }
}