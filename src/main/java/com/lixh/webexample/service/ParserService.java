package com.lixh.webexample.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lixh.webexample.data.entity.*;
import com.lixh.webexample.data.enums.FieldType;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import com.lixh.webexample.service.ai.ParserAssistant;
import com.lixh.webexample.service.freemarker.FreeMarkerService;

import lombok.extern.slf4j.Slf4j;

/**
 * 解析服务类
 */
@Service
@Slf4j
public class ParserService {

    private final ParserAssistant parserAssistant;

    private final FieldConfigService fieldConfigService;

    private final FieldMappingService fieldMappingService;

    private final ParseDetailService parseDetailService;

    private final ParseHistoryService parseHistoryService;

    private final UserInputService userInputService;

    private final FreeMarkerService freeMarkerService;

    private final ObjectMapper objectMapper;

    private final FewshotService fewshotService;

    @Autowired
    public ParserService(ParserAssistant parserAssistant,
                         FieldConfigService fieldConfigService,
                         FieldMappingService fieldMappingService,
                         ParseDetailService parseDetailService,
                         ParseHistoryService parseHistoryService,
                         UserInputService userInputService,
                         FreeMarkerService freeMarkerService,
                         ObjectMapper objectMapper,
                         FewshotService fewshotService) {
        this.parserAssistant = parserAssistant;
        this.fieldConfigService = fieldConfigService;
        this.fieldMappingService = fieldMappingService;
        this.parseDetailService = parseDetailService;
        this.parseHistoryService = parseHistoryService;
        this.userInputService = userInputService;
        this.freeMarkerService = freeMarkerService;
        this.objectMapper = objectMapper;
        this.fewshotService = fewshotService;
    }

    /**
     * 获取待解析字段的ID
     * 查找name="待解析"且type="系统"的字段配置，返回其ID
     * 这个字段的值将作为唯一的待解析文本
     *
     * @param materialType 物料类型
     * @return 待解析字段的ID
     */
    public Long getParseFieldId(String materialType) {
        List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(materialType);
        for (FieldConfigPo fieldConfig : fieldConfigs) {
            if ("待解析".equals(fieldConfig.getName()) && "系统".equals(fieldConfig.getType())) {
                return fieldConfig.getId();
            }
        }
        return null;
    }

    /**
     * 解析一行数据中的字段，使用分层迭代解析
     *
     * @param parseHistoryId 解析历史ID
     * @param rowNum         行号
     * @return 解析结果，key为字段名，value为字段值
     */
    public Map<String, String> parseRow(Long parseHistoryId, Integer rowNum) {
        // 1. 获取解析历史记录
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
        if (parseHistory == null) {
            throw new IllegalStateException("未找到解析历史记录");
        }

        // 2. 获取字段配置
        List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());

        // 3. 构建字段拓扑结构
        List<List<FieldConfigPo>> fieldTopology = buildFieldTopology(fieldConfigs);

        return parseRow(parseHistoryId, rowNum, fieldTopology, 0);
    }

    /**
     * 解析一行数据中的字段，从指定层级开始解析
     *
     * @param parseHistoryId 解析历史ID
     * @param rowNum         行号
     * @param fieldTopology  字段拓扑结构
     * @param startLevel     开始解析的层级
     * @return 解析结果，key为字段名，value为字段值
     */
    public Map<String, String> parseRow(Long parseHistoryId, Integer rowNum, List<List<FieldConfigPo>> fieldTopology,
                                        int startLevel) {
        // 1. 获取解析历史记录
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
        if (parseHistory == null) {
            throw new IllegalStateException("未找到解析历史记录");
        }

        // 2. 获取解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(parseHistoryId, rowNum);

        // 4. 创建已解析字段的映射，用于存储解析结果
        Map<String, String> extractedFields = new HashMap<>();

        // 5. 初始化已解析字段（所有已经有值的字段，不包括待解析字段）
        Long parseFieldId = getParseFieldId(parseHistory.getMaterialType());
        for (ParseDetailPo detail : parseDetails) {
            if (detail.getFieldValue() != null && !detail.getFieldValue().isEmpty()
                    && (parseFieldId == null || !parseFieldId.equals(detail.getFieldConfigId()))) {
                // 获取字段配置
                FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(detail.getFieldConfigId());
                if (fieldConfig != null) {
                    extractedFields.put(fieldConfig.getName(), detail.getFieldValue());
                } else {
                    log.warn("未找到字段配置，ID: {}", detail.getFieldConfigId());
                }
            }
        }

        // 6. 从指定层级开始解析字段
        for (int levelIndex = startLevel; levelIndex < fieldTopology.size(); levelIndex++) {
            List<FieldConfigPo> level = fieldTopology.get(levelIndex);

            // 过滤出当前层级中类型为AI推断的字段
            List<FieldConfigPo> aiInferenceFields = level.stream()
                    .filter(field -> FieldType.AI_INFERENCE.getValue().equals(field.getType()))
                    .collect(Collectors.toList());

            if (aiInferenceFields.isEmpty()) {
                continue;
            }

            // 生成当前层级的解析提示
            String prompt = generatePromptForLevel(parseHistory, rowNum, aiInferenceFields, extractedFields,
                    parseFieldId, parseDetails);

            // 解析当前层级的字段
            String memoryId = parseHistoryId + "_" + rowNum + "_level_" + levelIndex;
            String response = parserAssistant.parse(prompt, memoryId);
            log.info("AI解析响应 (层级 {}): {}", levelIndex, response);

            // 解析响应
            Map<String, String> levelResult = parseResponse(response);

            // 更新已解析字段
            extractedFields.putAll(levelResult);

            // 更新解析详情
            updateParseDetails(parseHistoryId, rowNum, levelResult);

            // 检查当前层级是否有字段解析失败（为空）
            boolean hasEmptyResult = levelResult.values().stream()
                    .anyMatch(value -> value == null || value.isEmpty());

            // 如果有字段解析失败，停止解析
            if (hasEmptyResult) {
                log.info("层级 {} 有字段解析失败，停止解析", levelIndex);
                break;
            }
        }

        return extractedFields;
    }

    /**
     * 生成解析提示
     *
     * @param parseHistoryId 解析历史ID
     * @param rowNum         行号
     * @return 解析提示
     */
    public String generatePrompt(Long parseHistoryId, Integer rowNum) {
        // 1. 获取解析历史记录
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
        if (parseHistory == null) {
            throw new IllegalStateException("未找到解析历史记录");
        }

        // 2. 获取字段映射关系
        List<FieldMappingPo> fieldMappings = fieldMappingService.getByParseHistoryId(parseHistoryId);
        if (fieldMappings.isEmpty()) {
            throw new IllegalStateException("未找到字段映射关系");
        }

        // 3. 获取解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(parseHistoryId, rowNum);
        if (parseDetails.isEmpty()) {
            throw new IllegalStateException("未找到解析详情");
        }

        // 4. 获取待解析字段的值
        Long parseFieldId = getParseFieldId(parseHistory.getMaterialType());

        // 5. 获取字段配置列表
        List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());

        // 6. 创建字段配置ID到解析详情的映射
        Map<Long, ParseDetailPo> fieldConfigToDetailMap = parseDetails.stream()
                .collect(Collectors.toMap(
                        ParseDetailPo::getFieldConfigId,
                        detail -> detail,
                        (existing, replacement) -> existing // 如果有重复，保留第一个
                ));

        // 7. 获取已解析的字段（所有已经有值的字段，不包括待解析字段）
        Map<String, String> extractedFields = new HashMap<>();
        for (ParseDetailPo detail : parseDetails) {
            if (detail.getFieldValue() != null && !detail.getFieldValue().isEmpty()
                    && (parseFieldId == null || !parseFieldId.equals(detail.getFieldConfigId()))) {
                // 获取字段配置
                FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(detail.getFieldConfigId());
                if (fieldConfig != null) {
                    extractedFields.put(fieldConfig.getName(), detail.getFieldValue());
                } else {
                    log.warn("未找到字段配置，ID: {}", detail.getFieldConfigId());
                }
            }
        }

        // 8. 获取需要解析的字段（类型是AI推断且对应的Detail中没有值的字段）
        List<FieldConfigPo> fieldsToExtractList = fieldConfigs.stream()
                .filter(config -> FieldType.AI_INFERENCE.getValue().equals(config.getType())) // 只考虑AI推断类型的字段
                .filter(config -> {
                    Optional<ParseDetailPo> detail = Optional.ofNullable(fieldConfigToDetailMap.get(config.getId()));
                    return detail.isEmpty() || detail.get().getFieldValue() == null
                            || detail.get().getFieldValue().isEmpty();
                })
                .filter(config -> parseFieldId == null || !parseFieldId.equals(config.getId())) // 排除待解析字段
                .collect(Collectors.toList());

        // 9. 调用generatePromptForLevel方法生成提示
        return generatePromptForLevel(parseHistory, rowNum, fieldsToExtractList, extractedFields, parseFieldId,
                parseDetails);
    }

    /**
     * 为特定层级生成解析提示
     *
     * @param parseHistory    解析历史
     * @param rowNum          行号
     * @param fieldsToExtract 需要解析的字段列表
     * @param extractedFields 已解析的字段映射
     * @param parseFieldId    待解析字段ID
     * @param parseDetails    解析详情列表
     * @return 解析提示
     */
    private String generatePromptForLevel(ParseHistoryPo parseHistory, Integer rowNum,
                                          List<FieldConfigPo> fieldsToExtract,
                                          Map<String, String> extractedFields,
                                          Long parseFieldId,
                                          List<ParseDetailPo> parseDetails) {
        // 1. 获取用户输入
        List<UserInputPo> userInputs = userInputService.getLatestUserInputs(parseHistory.getId());

        // 2. 获取待解析字段的值
        String parseFieldValue = null;

        if (parseFieldId != null) {
            for (ParseDetailPo detail : parseDetails) {
                if (parseFieldId.equals(detail.getFieldConfigId())) {
                    parseFieldValue = detail.getFieldValue();
                    break;
                }
            }
        }

        // 3. 预处理用户输入信息
        String userInputsText = "";
        if (!userInputs.isEmpty()) {
            StringBuilder sb = new StringBuilder("用户提供的额外信息：\n");
            for (UserInputPo input : userInputs) {
                sb.append("    ").append(input.getUserInput()).append("\n");
            }
            userInputsText = sb.toString();
        }

        // 4. 预处理已解析的字段
        String extractedFieldsText = "";
        if (!extractedFields.isEmpty()) {
            StringBuilder sb = new StringBuilder("已解析的字段：\n");
            for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                sb.append("    - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            extractedFieldsText = sb.toString();
        }

        // 5. 预处理需要解析的AI推断字段
        String fieldsToExtractText;
        if (!fieldsToExtract.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (FieldConfigPo field : fieldsToExtract) {
                sb.append("    - ").append(field.getName());
                if (field.getDescription() != null && !field.getDescription().trim().isEmpty()) {
                    sb.append("（").append(field.getDescription().trim()).append("）");
                }
                sb.append("\n");
            }
            fieldsToExtractText = sb.toString();
        } else {
            fieldsToExtractText = "    没有需要解析的AI推断字段\n";
        }

        // 6. 预先构建JSON示例格式
        String jsonExample;
        try {
            Map<String, Object> exampleJson = new HashMap<>();
            for (FieldConfigPo field : fieldsToExtract) {
                exampleJson.put(field.getName(), "提取的值");
            }
            jsonExample = objectMapper.writeValueAsString(exampleJson);
        } catch (Exception e) {
            log.warn("构建JSON示例失败: {}", e.getMessage());
            jsonExample = "{}";
        }

        // 7. 构建提示模板参数
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("parseFieldValue", parseFieldValue);
        templateParams.put("userInputsText", userInputsText);
        templateParams.put("extractedFieldsText", extractedFieldsText);
        templateParams.put("fieldsToExtractText", fieldsToExtractText);
        templateParams.put("jsonExample", jsonExample);

        // 8. 获取最相似的2个fewshot示例
        if (parseFieldValue != null && !parseFieldValue.isEmpty()) {
            List<FewshotService.FewshotExample> fewshotExamples = fewshotService.findSimilarExamples(parseFieldValue,
                    2);
            if (!fewshotExamples.isEmpty()) {
                templateParams.put("fewshotExamples", fewshotExamples);
            }
        }

        // 9. 使用FreeMarker处理模板
        return freeMarkerService.processTemplate("templates/prompts/field-parser-simple.ftl", templateParams);
    }

    /**
     * 解析响应
     *
     * @param response 响应
     * @return 解析结果，key为字段名，value为字段值
     */
    private Map<String, String> parseResponse(String response) {
        // 尝试提取```json和```之间的内容
        String jsonContent = extractJsonContent(response);

        // 尝试解析JSON
        try {
            return objectMapper.readValue(jsonContent, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("JSON解析失败，尝试使用正则表达式解析: {}", e.getMessage());
            // 如果解析失败，尝试使用正则表达式解析
            Map<String, String> result = new HashMap<>();
            // 使用正则表达式解析响应
            Pattern pattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(jsonContent);
            while (matcher.find()) {
                result.put(matcher.group(1), matcher.group(2));
            }
            return result;
        }
    }

    /**
     * 从响应中提取JSON内容
     *
     * @param response 响应
     * @return JSON内容
     */
    private String extractJsonContent(String response) {
        // 尝试提取```json和```之间的内容
        Pattern jsonPattern = Pattern.compile("```json\\s*(.+?)\\s*```", Pattern.DOTALL);
        Matcher jsonMatcher = jsonPattern.matcher(response);
        if (jsonMatcher.find()) {
            return jsonMatcher.group(1);
        }

        // 如果没有找到```json标记，则返回原始响应
        return response;
    }

    /**
     * 更新解析详情
     *
     * @param parseHistoryId 解析历史ID
     * @param rowNum         行号
     * @param result         解析结果
     */
    private void updateParseDetails(Long parseHistoryId, Integer rowNum, Map<String, String> result) {
        // 1. 获取解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(parseHistoryId, rowNum);
        if (parseDetails.isEmpty()) {
            throw new IllegalStateException("未找到解析详情");
        }

        // 2. 筛选出当前行的解析详情
        List<ParseDetailPo> rowParseDetails = parseDetails;

        // 3. 获取解析历史记录
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
        if (parseHistory == null) {
            throw new IllegalStateException("未找到解析历史记录");
        }

        // 4. 获取字段配置列表
        List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());

        // 创建字段配置ID到解析详情的映射
        Map<Long, ParseDetailPo> fieldConfigIdToDetailMap = rowParseDetails.stream()
                .collect(Collectors.toMap(
                        ParseDetailPo::getFieldConfigId,
                        detail -> detail,
                        (existing, replacement) -> existing // 如果有重复，保留第一个
                ));

        // 5. 更新解析详情（只更新AI推断类型的字段）
        for (FieldConfigPo fieldConfig : fieldConfigs) {
            // 检查字段类型是否为AI推断
            if (fieldConfig != null &&
                    FieldType.AI_INFERENCE.getValue().equals(fieldConfig.getType())) {

                String fieldName = fieldConfig.getName();
                if (result.containsKey(fieldName)) {
                    String fieldValue = result.get(fieldName);

                    // 检查是否已存在该字段的解析详情
                    ParseDetailPo detail = fieldConfigIdToDetailMap.get(fieldConfig.getId());

                    if (detail != null) {
                        // 更新现有的解析详情
                        detail.setFieldValue(fieldValue);
                        detail.setStatus(ParseDetailStatus.PENDING); // 设置状态为待确认
                        log.debug("更新字段 [{}] 的值为: {}", fieldName, fieldValue);
                        parseDetailService.updateParseDetail(detail);
                    } else {
                        // 创建新的解析详情
                        ParseDetailPo newDetail = new ParseDetailPo();
                        newDetail.setParseHistoryId(parseHistoryId);
                        newDetail.setFieldName(fieldName);
                        newDetail.setFieldValue(fieldValue);
                        newDetail.setFieldConfigId(fieldConfig.getId());
                        newDetail.setRowNum(rowNum);
                        newDetail.setStatus(ParseDetailStatus.PENDING); // 设置状态为待确认
                        newDetail.setCreateBy(parseHistory.getCreateBy());
                        newDetail.setUpdateBy(parseHistory.getUpdateBy());

                        log.debug("创建新字段 [{}] 的值为: {}", fieldName, fieldValue);
                        parseDetailService.addParseDetail(newDetail);
                    }
                }
            }
        }
    }

    /**
     * 构建字段的拓扑结构
     *
     * @param fieldConfigs 字段配置列表
     * @return 按层级分组的字段列表，每个层级是一个字段配置列表
     */
    private List<List<FieldConfigPo>> buildFieldTopology(List<FieldConfigPo> fieldConfigs) {
        // 结果列表，每个元素是一个层级的字段列表
        List<List<FieldConfigPo>> result = new ArrayList<>();

        // 按parentId分组
        Map<Long, List<FieldConfigPo>> fieldsByParentId = fieldConfigs.stream()
                .collect(Collectors.groupingBy(
                        field -> field.getParentId() == null ? 0L : field.getParentId()));

        // 从根节点开始构建拓扑结构
        List<FieldConfigPo> currentLevel = fieldsByParentId.getOrDefault(0L, new ArrayList<>());

        // 记录已处理的字段ID，用于检测循环依赖
        Set<Long> processedFieldIds = new HashSet<>();

        // 当前层级不为空时，继续构建下一层级
        while (!currentLevel.isEmpty()) {
            // 添加当前层级到结果
            result.add(currentLevel);

            // 记录当前层级的字段ID
            for (FieldConfigPo field : currentLevel) {
                processedFieldIds.add(field.getId());
            }

            // 获取下一层级的字段
            List<FieldConfigPo> nextLevel = new ArrayList<>();
            for (FieldConfigPo field : currentLevel) {
                List<FieldConfigPo> children = fieldsByParentId.getOrDefault(field.getId(), new ArrayList<>());
                // 过滤掉已处理的字段，避免循环依赖
                children = children.stream()
                        .filter(child -> !processedFieldIds.contains(child.getId()))
                        .toList();
                nextLevel.addAll(children);
            }

            // 更新当前层级
            currentLevel = nextLevel;
        }

        return result;
    }
}