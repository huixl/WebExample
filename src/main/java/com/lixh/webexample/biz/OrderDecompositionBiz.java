package com.lixh.webexample.biz;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lixh.webexample.config.UserContext;
import com.lixh.webexample.constant.ParseStatus;
import com.lixh.webexample.converter.OrderDecompositionConverter;
import com.lixh.webexample.data.entity.*;
import com.lixh.webexample.data.enums.FieldType;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import com.lixh.webexample.data.enums.RelationType;
import com.lixh.webexample.dto.request.ConfirmResultRequest;
import com.lixh.webexample.dto.request.FieldConfigRequest;
import com.lixh.webexample.dto.request.ParseSubmitRequest;
import com.lixh.webexample.dto.response.*;
import com.lixh.webexample.ex.BusinessException;
import com.lixh.webexample.service.*;
import com.lixh.webexample.util.ConcurrencyUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单拆解业务逻辑类
 */
@Component
@Slf4j
public class OrderDecompositionBiz {

    @Autowired
    private FieldConfigService fieldConfigService;

    @Autowired
    private ParseHistoryService parseHistoryService;

    @Autowired
    private ParseDetailService parseDetailService;

    @Autowired
    private UserInputService userInputService;

    @Autowired
    private OrderDecompositionConverter orderDecompositionConverter;

    @Autowired
    private FieldMappingService fieldMappingService;

    @Autowired
    private ParseTaskService parseTaskService;

    @Autowired
    private ParserService parserService;

    @Autowired
    private java.util.concurrent.Executor taskExecutor;

    @Autowired
    private ConcurrencyUtil concurrencyUtil;

    @Autowired
    private FewshotService fewshotService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private CalculationMappingService calculationMappingService;

    /**
     * 获取字段配置列表
     *
     * @param materialType 物料类型
     * @return 字段配置列表
     */
    public List<FieldConfigResponse> getFieldConfigs(String materialType) {
        List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(materialType);
        return fieldConfigs.stream().map(orderDecompositionConverter::toFieldConfigResponse)
                .collect(Collectors.toList());
    }

    /**
     * 添加字段配置
     *
     * @param request 字段配置请求
     * @return 添加结果
     */
    public FieldConfigResponse addFieldConfig(FieldConfigRequest request) {
        // 将请求转换为实体
        FieldConfigPo fieldConfig = orderDecompositionConverter.toFieldConfig(request);

        // 检查是否已存在同名同类型的字段
        FieldConfigPo existingConfig = fieldConfigService.addFieldConfig(fieldConfig);

        // 转换为响应
        FieldConfigResponse response = orderDecompositionConverter.toFieldConfigResponse(existingConfig);

        // 如果是已存在的字段，添加标记
        response.setIsExisting(existingConfig.getId() == null || !existingConfig.getId().equals(fieldConfig.getId()));

        return response;
    }

    /**
     * 更新字段配置
     *
     * @param id      字段配置ID
     * @param request 字段配置请求
     * @return 更新结果
     */
    public FieldConfigResponse updateFieldConfig(Long id, FieldConfigRequest request) {
        // 将请求转换为实体
        FieldConfigPo fieldConfig = orderDecompositionConverter.toFieldConfig(request);
        fieldConfig.setId(id);

        FieldConfigPo updatedConfig = fieldConfigService.updateFieldConfig(fieldConfig);

        if (updatedConfig == null) {
            throw new RuntimeException("字段配置不存在");
        }

        return orderDecompositionConverter.toFieldConfigResponse(updatedConfig);
    }

    /**
     * 删除字段配置
     *
     * @param id 字段配置ID
     * @return 删除结果
     */
    public DeleteFieldConfigResponse deleteFieldConfig(Long id) {
        boolean deleted = fieldConfigService.deleteFieldConfig(id);

        DeleteFieldConfigResponse response = new DeleteFieldConfigResponse();

        if (deleted) {
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            response.setMessage("系统默认字段不可删除");
            throw new RuntimeException("系统默认字段不可删除");
        }

        return response;
    }

    /**
     * 获取解析历史列表
     *
     * @param materialType 材料类型
     * @return 解析历史列表
     */
    public List<ParseHistoryResponse> getParseHistories(String materialType) {
        List<ParseHistoryPo> parseHistories = parseHistoryService.getParseHistories(materialType);
        return parseHistories.stream().map(orderDecompositionConverter::toParseHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取解析历史列表（支持排序和搜索）
     *
     * @param materialType 材料类型
     * @param keyword      搜索关键词（可选）
     * @param sortField    排序字段（可选）
     * @param sortOrder    排序方式（可选，asc或desc）
     * @return 解析历史列表
     */
    public List<ParseHistoryResponse> getParseHistories(String materialType, String keyword, String sortField,
            String sortOrder) {
        List<ParseHistoryPo> parseHistories = parseHistoryService.getParseHistories(materialType, keyword, sortField,
                sortOrder);
        return parseHistories.stream().map(orderDecompositionConverter::toParseHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取解析详情
     *
     * @param id 解析历史ID
     * @return 解析详情
     */
    public ParseHistoryResponse getParseDetail(Long id) {
        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            throw new RuntimeException("解析历史不存在");
        }

        // 如果解析状态为"解析中"，不允许查看详情
        if (parseHistory.getParseStatus() == ParseStatus.PARSING) {
            throw new RuntimeException("解析任务尚未完成，无法查看详情");
        }

        // 获取解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(id);

        // 获取字段映射
        List<FieldMappingPo> fieldMappings = fieldMappingService.getFieldMappings(id);

        // 转换为响应
        ParseHistoryResponse response = orderDecompositionConverter.toParseHistoryResponse(parseHistory);

        // 添加详情
        List<ParseHistoryResponse.ParseDetailDTO> detailDTOs = orderDecompositionConverter
                .toParseDetailDTOList(parseDetails);

        response.setDetails(detailDTOs);

        // 添加字段映射
        List<ParseHistoryResponse.FieldMappingDTO> fieldMappingDTOs = orderDecompositionConverter
                .toFieldMappingDTOList(fieldMappings);

        // 为每个字段映射DTO设置字段名称
        for (ParseHistoryResponse.FieldMappingDTO dto : fieldMappingDTOs) {
            if (dto.getFieldConfigId() != null) {
                FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(dto.getFieldConfigId());
                if (fieldConfig != null) {
                    dto.setFieldName(fieldConfig.getName());
                }
            }
        }

        response.setFieldMappings(fieldMappingDTOs);

        return response;
    }

    /**
     * 获取解析详情（支持分页）
     *
     * @param id          解析历史ID
     * @param pageSize    每页大小
     * @param pageNum     页码
     * @param onlyPending 是否只显示待确认状态的数据
     * @return 解析详情
     */
    public ParseHistoryResponse getParseDetailWithPagination(Long id, Integer pageSize, Integer pageNum,
            Boolean onlyPending) {
        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            throw new RuntimeException("解析历史不存在");
        }

        // 如果解析状态为"解析中"，不允许查看详情
        if (parseHistory.getParseStatus() == ParseStatus.PARSING) {
            throw new RuntimeException("解析任务尚未完成，无法查看详情");
        }

        // 获取解析详情（分页）
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetailsWithPagination(id, pageSize, pageNum,
                onlyPending);

        // 获取字段映射
        List<FieldMappingPo> fieldMappings = fieldMappingService.getFieldMappings(id);

        // 转换为响应
        ParseHistoryResponse response = orderDecompositionConverter.toParseHistoryResponse(parseHistory);

        // 添加详情
        List<ParseHistoryResponse.ParseDetailDTO> detailDTOs = orderDecompositionConverter
                .toParseDetailDTOList(parseDetails);

        response.setDetails(detailDTOs);

        // 添加字段映射
        List<ParseHistoryResponse.FieldMappingDTO> fieldMappingDTOs = orderDecompositionConverter
                .toFieldMappingDTOList(fieldMappings);

        // 为每个字段映射DTO设置字段名称
        for (ParseHistoryResponse.FieldMappingDTO dto : fieldMappingDTOs) {
            if (dto.getFieldConfigId() != null) {
                FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(dto.getFieldConfigId());
                if (fieldConfig != null) {
                    dto.setFieldName(fieldConfig.getName());
                }
            }
        }

        response.setFieldMappings(fieldMappingDTOs);

        // 添加分页信息
        response.setTotal(parseDetailService.countParseDetails(id, onlyPending));
        response.setPageSize(pageSize);
        response.setPageNum(pageNum);

        return response;
    }

    /**
     * 提交解析任务
     *
     * @param request 解析提交请求
     * @return 解析历史
     */
    public ParseHistoryResponse submitParse(ParseSubmitRequest request) {
        String userName = UserContext.getCurrentUsername();

        // 创建解析历史
        ParseHistoryPo parseHistory = orderDecompositionConverter.toParseHistory(request);

        // 设置自定义名称
        parseHistory.setCustomName(request.getCustomName());

        // 设置解析状态为"解析中"，不再根据additionalPrompt判断
        parseHistory.setParseStatus(ParseStatus.PARSING);

        parseHistory.setCreateBy(userName);
        parseHistory.setUpdateBy(userName);

        // 保存解析历史
        parseHistoryService.createParseHistory(parseHistory);

        // 保存字段映射关系
        if (request.getFieldMappings() != null && !request.getFieldMappings().isEmpty()) {
            List<FieldMappingPo> fieldMappings = new ArrayList<>();

            // 获取所有字段配置，用于查找字段配置ID
            List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());
            Map<String, Long> fieldNameToIdMap = new HashMap<>();
            for (FieldConfigPo config : fieldConfigs) {
                fieldNameToIdMap.put(config.getName(), config.getId());
            }

            for (ParseSubmitRequest.FieldMappingDTO mappingDTO : request.getFieldMappings()) {
                FieldMappingPo fieldMapping = new FieldMappingPo();
                fieldMapping.setParseHistoryId(parseHistory.getId());
                fieldMapping.setMetadataId(request.getParseMetadataId());
                fieldMapping.setExcelPosition(mappingDTO.getSourceField());
                fieldMapping.setMetadataFieldName(mappingDTO.getSourceField());

                // 根据目标字段名称查找对应的字段配置ID
                Long fieldConfigId = fieldNameToIdMap.get(mappingDTO.getTargetField());
                if (fieldConfigId != null) {
                    fieldMapping.setFieldConfigId(fieldConfigId);
                }

                fieldMapping.setCreateBy(userName);
                fieldMapping.setUpdateBy(userName);

                fieldMappings.add(fieldMapping);
            }

            // 批量保存字段映射
            fieldMappingService.batchAddFieldMappings(fieldMappings);
        }

        // 如果有additionalPrompt，保存到user-input表
        if (request.getAdditionalPrompt() != null && !request.getAdditionalPrompt().isEmpty()) {
            UserInputPo userInput = new UserInputPo();
            userInput.setParseHistoryId(parseHistory.getId());
            userInput.setUserInput(request.getAdditionalPrompt());
            userInput.setCreateBy(userName);
            userInput.setUpdateBy(userName);

            userInputService.addUserInput(userInput);
        }

        // 启动异步任务处理解析
        parseTaskService.startParseTask(parseHistory.getId());

        return orderDecompositionConverter.toParseHistoryResponse(parseHistory);
    }

    /**
     * 获取解析结果
     *
     * @param id 解析历史ID
     * @return 解析结果
     */
    public ParseResultResponse getParseResult(Long id) {
        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            throw new RuntimeException("解析历史不存在");
        }

        // 如果解析状态不是"解析成功"，返回错误信息
        if (parseHistory.getParseStatus() != ParseStatus.SUCCESS) {
            ParseResultResponse response = new ParseResultResponse();
            response.setId(parseHistory.getId());
            response.setMaterialType(parseHistory.getMaterialType());
            response.setParseStatus(parseHistory.getParseStatus());
            response.setCreateTime(parseHistory.getCreateTime());
            response.setUpdateTime(parseHistory.getUpdateTime());

            // 根据不同的解析状态设置不同的错误信息
            String errorMessage = "";
            if (parseHistory.getParseStatus() == ParseStatus.PARSING) {
                errorMessage = "解析任务正在进行中，请稍后查看结果";
            } else if (parseHistory.getParseStatus() == ParseStatus.FAILED) {
                errorMessage = parseHistory.getErrorMessage();
            } else if (parseHistory.getParseStatus() == ParseStatus.WAITING_FOR_INPUT) {
                errorMessage = "解析任务需要用户输入，请确认解析结果";
            }

            // 创建结果对象
            ParseResultResponse.ParseResultDataDTO resultData = new ParseResultResponse.ParseResultDataDTO();
            resultData.setParseTime(LocalDateTime.now().toString());
            resultData.setConfidence(0.0);

            // 创建错误字段
            ParseResultResponse.ParsedFieldDTO errorField = new ParseResultResponse.ParsedFieldDTO();
            errorField.setFieldName("error");
            errorField.setFieldValue(errorMessage);

            // 设置解析字段列表
            List<ParseResultResponse.ParsedFieldDTO> parsedFields = new ArrayList<>();
            parsedFields.add(errorField);
            resultData.setParsedFields(parsedFields);

            response.setResult(resultData);

            return response;
        }

        // 获取解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(id);

        // 构建响应
        ParseResultResponse response = orderDecompositionConverter.toParseResultResponse(parseHistory);

        // 构建解析结果
        ParseResultResponse.ParseResultDataDTO result = new ParseResultResponse.ParseResultDataDTO();

        // 构建解析字段
        List<ParseResultResponse.ParsedFieldDTO> parsedFields = orderDecompositionConverter
                .toParsedFieldDTOList(parseDetails);

        result.setParsedFields(parsedFields);
        result.setConfidence(0.95); // 示例值
        result.setParseTime("5秒"); // 示例值

        response.setResult(result);

        return response;
    }

    /**
     * 确认解析结果
     *
     * @param id      解析历史ID
     * @param request 确认请求
     * @return 确认结果响应
     */
    @Transactional(rollbackFor = Throwable.class)
    public ConfirmResultResponse confirmParseResult(Long id, ConfirmResultRequest request) {
        String userName = UserContext.getCurrentUsername();

        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            throw new RuntimeException("解析历史不存在");
        }

        // 检查解析历史的状态是否为"解析成功"或"等待用户输入"
        if (parseHistory.getParseStatus() != ParseStatus.SUCCESS
                && parseHistory.getParseStatus() != ParseStatus.WAITING_FOR_CALCULATION_CONFIRM
                && parseHistory.getParseStatus() != ParseStatus.WAITING_FOR_INPUT) {
            throw new BusinessException("只有解析成功或等待用户输入的任务才能确认结果");
        }

        // 检查解析历史的状态是否为"解析成功"，如果是则阻止任何更改
        if (parseHistory.getParseStatus() == ParseStatus.SUCCESS) {
            throw new BusinessException("解析任务已完成，不能再进行修改");
        }

        // 获取所有解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(id);
        if (parseDetails.isEmpty()) {
            throw new BusinessException("未找到解析详情");
        }

        // 创建字段配置ID到解析详情的映射，考虑行号
        Map<String, ParseDetailPo> detailMap = parseDetails.stream()
                .collect(Collectors.toMap(
                        detail -> detail.getFieldConfigId() + "_" + detail.getRowNum(),
                        Function.identity(),
                        (existing, replacement) -> existing));

        // 更新确认的字段状态
        List<ParseDetailPo> detailsToUpdate = new ArrayList<>();
        for (ConfirmResultRequest.ConfirmedFieldDTO field : request.getConfirmedFields()) {
            // 验证字段值是否有效
            if (StringUtils.isBlank(field.getFieldValue())) {
                log.error("字段值为空，字段名: {}", field.getFieldName());
                continue;
            }

            // 获取字段配置
            FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(field.getFieldConfigId());
            if (fieldConfig == null) {
                log.error("未找到字段配置，ID: {}", field.getFieldConfigId());
                throw new BusinessException("未找到字段配置: " + field.getFieldConfigId());
            }

            // 处理行号为空的情况
            Integer rowNum = field.getRowNum();
            if (rowNum == null) {
                log.error("行号为空，字段名: {}, 字段配置ID: {}", field.getFieldName(), field.getFieldConfigId());
                throw new BusinessException("行号不能为空: " + field.getFieldName());
            }

            // 查找现有的解析详情，使用字段配置ID和行号作为键
            String detailKey = field.getFieldConfigId() + "_" + rowNum;
            log.debug("构造详情键: {}", detailKey);
            ParseDetailPo existingDetail = detailMap.get(detailKey);

            if (existingDetail != null) {
                // 更新现有的解析详情
                existingDetail.setFieldValue(field.getFieldValue());
                existingDetail.setStatus(ParseDetailStatus.CONFIRMED);
                existingDetail.setUpdateBy(userName);
                existingDetail.setUpdateTime(LocalDateTime.now());
                detailsToUpdate.add(existingDetail);
            } else {
                // 创建新的解析详情
                ParseDetailPo newDetail = new ParseDetailPo();
                newDetail.setParseHistoryId(id);
                newDetail.setFieldName(fieldConfig.getName());
                newDetail.setFieldValue(field.getFieldValue());
                newDetail.setFieldConfigId(field.getFieldConfigId());
                newDetail.setRowNum(rowNum); // 使用处理过的 rowNum 变量
                newDetail.setStatus(ParseDetailStatus.CONFIRMED);
                newDetail.setCreateBy(userName);
                newDetail.setUpdateBy(userName);
                detailsToUpdate.add(newDetail);
            }
        }

        // 批量更新解析详情
        if (!detailsToUpdate.isEmpty()) {
            for (ParseDetailPo detail : detailsToUpdate) {
                if (detail.getId() != null) {
                    parseDetailService.updateParseDetail(detail);
                } else {
                    parseDetailService.addParseDetail(detail);
                }
            }

            // 检查是否有完全确认的行，如果有则存储为fewshot示例
            checkAndStoreFewshotExample(id, request, parseHistory);
        }

        // 确认解析历史
        parseHistory = parseHistoryService.confirmParseHistory(id, userName);

        // 构建响应
        ConfirmResultResponse response = orderDecompositionConverter.createSuccessConfirmResultResponse();
        response.setData(orderDecompositionConverter.toConfirmResultDataDTO(parseHistory));

        return response;
    }

    /**
     * 生成批次ID
     *
     * @param materialType 材料类型
     * @return 批次ID
     */
    private String generateBatchId(String materialType) {
        return materialType + "_" + System.currentTimeMillis();
    }

    /**
     * 创建数据库记录
     *
     * @param materialType  材料类型
     * @param batchId       批次ID
     * @param relationType  关系类型
     * @param parentFieldId 父字段ID
     * @param parentValue   父字段值
     * @param childFieldId  子字段ID
     * @param childValue    子字段值
     * @return 数据库记录
     */
    private DatabasePo createDatabaseRecord(String materialType, String batchId, String relationType,
            Long parentFieldId, String parentValue,
            Long childFieldId, String childValue) {
        DatabasePo record = new DatabasePo();
        record.setMaterialType(materialType);
        record.setBatchId(batchId);
        record.setRelationType(relationType);
        record.setParentFieldId(parentFieldId);
        record.setParentValue(parentValue);
        record.setChildFieldId(childFieldId);
        record.setChildValue(childValue);
        return record;
    }

    /**
     * 处理字段关系并创建数据库记录
     *
     * @param materialType  材料类型
     * @param fieldConfigs  字段配置列表
     * @param getFieldValue 获取字段值的函数
     * @return 数据库记录列表
     */
    private List<DatabasePo> processFieldRelationsAndCreateRecords(
            String materialType,
            List<FieldConfigPo> fieldConfigs,
            Function<Long, String> getFieldValue) {

        // 创建批次ID
        String batchId = generateBatchId(materialType);

        // 创建字段ID到字段配置的映射
        Map<Long, FieldConfigPo> fieldIdToConfigMap = fieldConfigs.stream()
                .collect(Collectors.toMap(FieldConfigPo::getId, Function.identity()));

        // 用于存储数据库记录
        List<DatabasePo> databaseRecords = new ArrayList<>();

        // 处理每个字段
        for (FieldConfigPo fieldConfig : fieldConfigs) {
            // 跳过系统字段
            if (FieldType.SYSTEM.getValue().equals(fieldConfig.getType())) {
                continue;
            }

            // 获取字段值
            String fieldValue = getFieldValue.apply(fieldConfig.getId());

            // 如果字段值为空，则跳过
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }

            // 如果字段有父节点，则创建父子关系记录
            if (fieldConfig.getParentId() != null && fieldConfig.getParentId() > 0) {
                FieldConfigPo parentConfig = fieldIdToConfigMap.get(fieldConfig.getParentId());
                if (parentConfig != null) {
                    // 获取父字段值
                    String parentValue = getFieldValue.apply(parentConfig.getId());

                    // 如果父字段值不为空，则创建父子关系记录
                    if (parentValue != null && !parentValue.isEmpty()) {
                        DatabasePo record = createDatabaseRecord(
                                materialType,
                                batchId,
                                RelationType.PARENT_CHILD.getValue(),
                                parentConfig.getId(),
                                parentValue,
                                fieldConfig.getId(),
                                fieldValue);

                        databaseRecords.add(record);
                    }
                }
            } else {
                // 创建无关系记录
                DatabasePo record = createDatabaseRecord(
                        materialType,
                        batchId,
                        RelationType.NO_RELATION.getValue(),
                        fieldConfig.getId(),
                        fieldValue,
                        null,
                        "");

                databaseRecords.add(record);
            }
        }

        return databaseRecords;
    }

    @Async
    protected void checkAndStoreFewshotExample(Long parseHistoryId, ConfirmResultRequest request,
            ParseHistoryPo parseHistory) {
        try {
            // 获取当前确认的行号
            Set<Integer> confirmedRowNums = request.getConfirmedFields().stream()
                    .map(ConfirmResultRequest.ConfirmedFieldDTO::getRowNum)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 获取所有字段配置
            List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());

            // 对每个行号进行检查
            for (Integer rowNum : confirmedRowNums) {
                // 获取该行的所有解析详情
                List<ParseDetailPo> rowDetails = parseDetailService.getParseDetails(parseHistoryId, rowNum);

                // 获取待解析字段ID
                Long parseFieldId = parserService.getParseFieldId(parseHistory.getMaterialType());

                // 获取待解析字段的值
                String parseFieldValue = null;
                for (ParseDetailPo detail : rowDetails) {
                    if (parseFieldId != null && parseFieldId.equals(detail.getFieldConfigId())) {
                        parseFieldValue = detail.getFieldValue();
                        break;
                    }
                }

                // 如果没有待解析字段的值，则跳过
                if (parseFieldValue == null || parseFieldValue.isEmpty()) {
                    continue;
                }

                // 检查该行是否所有AI推断字段都已确认
                boolean allConfirmed = true;
                Map<String, String> extractedFields = new HashMap<>();

                for (FieldConfigPo fieldConfig : fieldConfigs) {
                    // 只检查AI推断字段
                    if (!FieldType.AI_INFERENCE.getValue().equals(fieldConfig.getType())) {
                        continue;
                    }

                    // 跳过待解析字段
                    if (parseFieldId != null && parseFieldId.equals(fieldConfig.getId())) {
                        continue;
                    }

                    // 查找该字段的解析详情
                    boolean fieldConfirmed = false;
                    for (ParseDetailPo detail : rowDetails) {
                        if (fieldConfig.getId().equals(detail.getFieldConfigId())) {
                            // 检查字段是否已确认
                            if (ParseDetailStatus.CONFIRMED.equals(detail.getStatus()) &&
                                    detail.getFieldValue() != null && !detail.getFieldValue().isEmpty()) {
                                fieldConfirmed = true;
                                extractedFields.put(fieldConfig.getName(), detail.getFieldValue());
                            }
                            break;
                        }
                    }

                    // 如果有任何字段未确认，则该行未完全确认
                    if (!fieldConfirmed) {
                        allConfirmed = false;
                        break;
                    }
                }

                // 如果所有字段都已确认，则存储为fewshot示例并存储到数据库
                if (allConfirmed && !extractedFields.isEmpty()) {
                    // 存储为fewshot示例
                    fewshotService.storeFewshotExample(parseFieldValue, extractedFields);
                    log.info("已存储fewshot示例，行号: {}", rowNum);

                    // 创建获取字段值的函数
                    Map<Long, String> fieldValueMap = new HashMap<>();
                    for (ParseDetailPo detail : rowDetails) {
                        if (detail.getFieldValue() != null && !detail.getFieldValue().isEmpty()) {
                            fieldValueMap.put(detail.getFieldConfigId(), detail.getFieldValue());
                        }
                    }

                    Function<Long, String> getFieldValue = fieldId -> fieldValueMap.getOrDefault(fieldId, "");

                    // 处理字段关系并创建数据库记录
                    List<DatabasePo> databaseRecords = processFieldRelationsAndCreateRecords(
                            parseHistory.getMaterialType(),
                            fieldConfigs,
                            getFieldValue);

                    // 批量保存到数据库
                    if (!databaseRecords.isEmpty()) {
                        int savedCount = databaseService.batchSave(databaseRecords);
                        log.info("已存储{}条数据到数据库，行号: {}", savedCount, rowNum);
                    }
                }
            }
        } catch (Exception e) {
            log.error("存储fewshot示例或数据库记录失败", e);
        }
    }

    /**
     * 完成解析结果
     *
     * @param id 解析历史ID
     * @return 完成解析结果响应
     */
    public CompleteParseResultResponse completeParseResult(Long id) {
        String userName = UserContext.getCurrentUsername();

        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            throw new RuntimeException("解析历史不存在");
        }

        // 检查解析历史的状态
        if (parseHistory.getParseStatus() == ParseStatus.SUCCESS) {
            throw new BusinessException("解析任务已完成，不能再进行修改");
        } else if (parseHistory.getParseStatus() != ParseStatus.WAITING_FOR_INPUT) {
            throw new BusinessException("只有等待用户输入的任务才能完成解析");
        }

        // 获取所有解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(id);

        // 先获取所有FieldConfig
        List<FieldConfigPo> allFieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());
        Set<Long> validFieldConfigIds = allFieldConfigs.stream()
                .map(FieldConfigPo::getId)
                .collect(Collectors.toSet());

        // 排除掉FieldConfig已经不存在的字段
        parseDetails = parseDetails.stream()
                .filter(detail -> validFieldConfigIds.contains(detail.getFieldConfigId()))
                .toList();

        // 检查是否所有详情都已确认
        boolean allConfirmed = parseDetails.stream()
                .allMatch(detail -> ParseDetailStatus.CONFIRMED.equals(detail.getStatus()));

        if (!allConfirmed) {
            throw new BusinessException("存在未确认的解析详情，请先确认所有详情");
        }

        // 批量更新所有解析详情状态为SUCCESS
        parseDetailService.batchUpdateStatus(id, ParseDetailStatus.SUCCESS, userName);

        // 更新解析历史状态为CALCULATING
        parseHistory.setParseStatus(ParseStatus.CALCULATING);
        parseHistory.setUpdateBy(userName);
        parseHistory.setUpdateTime(LocalDateTime.now());
        parseHistoryService.updateParseHistory(parseHistory);
        
        // 同步执行计算任务
        try {
            boolean calculationSuccess = calculationMappingService.processCalculation(id);
            if (!calculationSuccess) {
                log.error("计算处理失败");
            }
        } catch (Exception e) {
            log.error("计算处理失败", e);
        }

        // 构建响应
        CompleteParseResultResponse response = new CompleteParseResultResponse();
        CompleteParseResultResponse.CompleteParseResultDataDTO dataDTO = new CompleteParseResultResponse.CompleteParseResultDataDTO();

        dataDTO.setId(parseHistory.getId());
        dataDTO.setMaterialType(parseHistory.getMaterialType());
        dataDTO.setParseStatus(parseHistory.getParseStatus());
        dataDTO.setCreateTime(parseHistory.getCreateTime());
        dataDTO.setUpdateTime(parseHistory.getUpdateTime());
        dataDTO.setConfirmTime(parseHistory.getConfirmTime());
        dataDTO.setConfirmedBy(parseHistory.getConfirmedBy());

        response.setData(dataDTO);

        return response;
    }

    /**
     * 删除解析行
     *
     * @param id     解析历史ID
     * @param rowNum 行号
     * @return 删除结果
     */
    public BaseResponse deleteParseRow(Long id, Integer rowNum) {
        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            return BaseResponse.error("解析历史不存在");
        }

        // 检查解析历史的状态
        if (parseHistory.getParseStatus() == ParseStatus.SUCCESS) {
            return BaseResponse.error("解析任务已完成，不能再进行修改");
        }

        // 获取指定行号的解析详情
        List<ParseDetailPo> rowDetails = parseDetailService.getParseDetails(id, rowNum);

        if (rowDetails.isEmpty()) {
            return BaseResponse.error("未找到指定行号的解析详情");
        }

        // 删除指定行号的所有解析详情
        parseDetailService.batchRemove(rowDetails);

        return BaseResponse.success("删除行成功");
    }

    /**
     * 添加解析行
     *
     * @param id      解析历史ID
     * @param request 行数据
     * @return 添加结果
     */
    public BaseResponse addParseRow(Long id, Map<String, Object> request) {
        String userName = UserContext.getCurrentUsername();

        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            return BaseResponse.error("解析历史不存在");
        }

        // 检查解析历史的状态
        if (parseHistory.getParseStatus() == ParseStatus.SUCCESS) {
            return BaseResponse.error("解析任务已完成，不能再进行修改");
        }

        // 获取所有解析详情
        List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(id);

        // 计算新行号
        int maxRowNum = 0;
        if (!parseDetails.isEmpty()) {
            maxRowNum = parseDetails.stream()
                    .mapToInt(ParseDetailPo::getRowNum)
                    .max()
                    .orElse(0);
        }
        int newRowNum = maxRowNum + 1;

        // 获取字段配置列表
        List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());

        if (fieldConfigs.isEmpty()) {
            return BaseResponse.error("未找到字段配置");
        }

        // 创建新行的解析详情
        List<ParseDetailPo> newDetails = new ArrayList<>();

        for (FieldConfigPo fieldConfig : fieldConfigs) {
            ParseDetailPo detail = new ParseDetailPo();
            detail.setParseHistoryId(id);
            detail.setFieldName(fieldConfig.getName());
            detail.setFieldValue(""); // 默认为空值
            detail.setFieldConfigId(fieldConfig.getId());
            detail.setRowNum(newRowNum);
            detail.setStatus(ParseDetailStatus.PENDING);
            detail.setCreateBy(userName);
            detail.setUpdateBy(userName);
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            detail.setDeleted(0);

            // 如果请求中包含该字段的值，则设置
            if (request.containsKey(fieldConfig.getName())) {
                Object value = request.get(fieldConfig.getName());
                if (value != null) {
                    detail.setFieldValue(value.toString());
                }
            }

            newDetails.add(detail);
        }

        // 批量保存新行的解析详情
        parseDetailService.batchSave(newDetails);

        return BaseResponse.success("添加行成功");
    }

    /**
     * 删除解析历史
     * <p>
     * 使用事务确保删除操作的原子性，同时删除解析历史及其相关的解析数据和字段映射
     *
     * @param id 解析历史ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Throwable.class)
    public BaseResponse deleteParseHistory(Long id) {
        log.info("删除解析历史，ID: {}", id);

        try {
            // 1. 删除解析详情
            parseDetailService.deleteParseDetails(id);

            // 2. 删除字段映射
            fieldMappingService.deleteFieldMappings(id);

            // 3. 删除用户输入记录（如果有）
            userInputService.deleteUserInputs(id);

            // 4. 删除解析历史
            parseHistoryService.deleteParseHistory(id);

            BaseResponse response = new BaseResponse();
            response.setSuccess(true);
            response.setMessage("删除成功");
            return response;
        } catch (Exception e) {
            log.error("删除解析历史失败，ID: {}", id, e);
            throw new RuntimeException("删除解析历史失败", e);
        }
    }

    /**
     * 清空解析结果中的特定列
     *
     * @param parseHistoryId 解析历史ID
     * @param fieldConfigId  字段配置ID
     * @return 操作结果
     */
    @Transactional(rollbackFor = Throwable.class)
    public BaseResponse clearParseColumn(Long parseHistoryId, Long fieldConfigId) {
        // 检查解析历史是否存在
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
        if (parseHistory == null) {
            throw new BusinessException("解析历史不存在");
        }

        // 检查解析历史状态
        if (parseHistory.getParseStatus() == ParseStatus.SUCCESS) {
            throw new BusinessException("解析已完成，无法修改");
        }

        // 检查字段配置是否存在
        FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(fieldConfigId);
        if (fieldConfig == null) {
            throw new BusinessException("字段配置不存在");
        }

        // 获取该列的所有解析详情
        List<ParseDetailPo> details = parseDetailService.getParseDetailsByParseHistoryIdAndFieldConfigId(
                parseHistoryId, fieldConfigId);

        // 清空每个详情的值
        for (ParseDetailPo detail : details) {
            detail.setFieldValue("");
            detail.setStatus(ParseDetailStatus.PENDING);
        }

        // 批量更新
        parseDetailService.batchUpdateParseDetails(details);

        // 返回成功响应
        BaseResponse response = new BaseResponse();
        response.setSuccess(true);
        response.setMessage("列清空成功");
        return response;
    }

    /**
     * 继续使用AI推断
     *
     * @param parseHistoryId 解析历史ID
     * @param userInput      用户输入
     * @return 操作结果
     */
    @Transactional(rollbackFor = Throwable.class)
    public BaseResponse continueAiInference(Long parseHistoryId, String userInput) {
        // 1. 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
        if (parseHistory == null) {
            throw new RuntimeException("解析历史不存在");
        }

        // 2. 保存用户输入
        UserInputPo userInputPo = new UserInputPo();
        userInputPo.setParseHistoryId(parseHistoryId);
        userInputPo.setUserInput(userInput);
        userInputService.addUserInput(userInputPo);

        // 3. 更新解析状态为"解析中"
        updateParseStatus(parseHistoryId, ParseStatus.PARSING, null);

        // 4. 启动异步任务
        startAiInferenceTask(parseHistoryId);

        // 5. 返回成功响应
        BaseResponse response = new BaseResponse();
        response.setSuccess(true);
        response.setMessage("AI推断任务已启动");
        return response;
    }

    /**
     * 启动AI推断任务
     *
     * @param parseHistoryId 解析历史ID
     */
    private void startAiInferenceTask(Long parseHistoryId) {
        // 使用线程池执行异步任务
        taskExecutor.execute(() -> {
            try {
                Thread.sleep(1000); // 一秒钟后再启动
                processAiInferenceTask(parseHistoryId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                log.error("启动AI推断任务时被中断", e);
                // 更新解析状态为"失败"
                updateParseStatus(parseHistoryId, ParseStatus.FAILED, "启动AI推断任务时被中断");
            }
        });
    }

    /**
     * 处理AI推断任务
     *
     * @param parseHistoryId 解析历史ID
     */
    private void processAiInferenceTask(Long parseHistoryId) {
        log.info("开始处理AI推断任务，历史ID: {}", parseHistoryId);
        try {
            // 1. 获取解析历史
            ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(parseHistoryId);
            if (parseHistory == null) {
                throw new IllegalStateException("未找到解析历史记录");
            }

            // 2. 获取字段配置
            List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(parseHistory.getMaterialType());

            // 3. 构建字段拓扑结构
            List<List<FieldConfigPo>> fieldTopology = buildFieldTopology(fieldConfigs);

            // 4. 获取所有行号
            List<Integer> rowNums = parseDetailService.getParseDetails(parseHistoryId).stream()
                    .map(ParseDetailPo::getRowNum)
                    .distinct()
                    .toList();

            // 5. 获取所有AI推断类型的字段配置
            List<FieldConfigPo> aiInferenceFields = fieldConfigs.stream()
                    .filter(field -> com.lixh.webexample.data.enums.FieldType.AI_INFERENCE.getValue()
                            .equals(field.getType()))
                    .toList();

            // 如果没有AI推断字段，直接返回
            if (aiInferenceFields.isEmpty()) {
                log.info("没有需要AI推断的字段，历史ID: {}", parseHistoryId);
                updateParseStatus(parseHistoryId, ParseStatus.SUCCESS, null);
                return;
            }

            // 6. 使用并发工具类处理每一行数据
            try {
                concurrencyUtil.executeWithConcurrencyControl(
                        rowNums,
                        rowNum -> {
                            try {
                                // 获取解析详情
                                List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(parseHistoryId,
                                        rowNum);

                                // 创建字段ID到解析详情的映射
                                Map<Long, ParseDetailPo> fieldIdToDetailMap = parseDetails.stream()
                                        .collect(Collectors.toMap(ParseDetailPo::getFieldConfigId, detail -> detail,
                                                (existing, replacement) -> existing));

                                // 计算第一个未解析的层级
                                int firstUnparsedLevel = findFirstUnparsedLevel(fieldTopology, fieldIdToDetailMap);

                                // 如果所有层级都已解析完成，记录日志并返回
                                if (firstUnparsedLevel >= fieldTopology.size()) {
                                    log.info("行 {} 的所有层级都已解析完成", rowNum);
                                    return;
                                }

                                // 从第一个未解析的层级开始解析
                                log.info("开始解析行 {} 的层级 {}", rowNum, firstUnparsedLevel);
                                parserService.parseRow(parseHistoryId, rowNum, fieldTopology, firstUnparsedLevel);
                            } catch (Exception e) {
                                log.error("行 {} 推断失败: {}", rowNum, e.getMessage(), e);
                            }
                        },
                        ConcurrencyUtil.ConcurrencyType.AI_INFERENCE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("AI推断任务被中断，历史ID: {}", parseHistoryId, e);
                updateParseStatus(parseHistoryId, ParseStatus.FAILED, "AI推断任务被中断");
                return;
            }

            // 7. 更新解析状态为"等待用户输入"
            updateParseStatus(parseHistoryId, ParseStatus.WAITING_FOR_INPUT, null);
            log.info("AI推断任务已完成，等待用户确认，历史ID: {}", parseHistoryId);
        } catch (Exception e) {
            log.error("处理AI推断任务出错，历史ID: {}", parseHistoryId, e);
            // 更新解析状态为"失败"
            updateParseStatus(parseHistoryId, ParseStatus.FAILED, e.getMessage());
        }
    }

    /**
     * 查找第一个未解析的层级
     *
     * @param fieldTopology      字段拓扑结构
     * @param fieldIdToDetailMap 字段ID到解析详情的映射
     * @return 第一个未解析的层级索引，如果所有层级都已解析完成，则返回 fieldTopology.size()
     */
    private int findFirstUnparsedLevel(List<List<FieldConfigPo>> fieldTopology,
            Map<Long, ParseDetailPo> fieldIdToDetailMap) {
        for (int levelIndex = 0; levelIndex < fieldTopology.size(); levelIndex++) {
            List<FieldConfigPo> level = fieldTopology.get(levelIndex);

            // 过滤出当前层级中类型为AI推断的字段
            List<FieldConfigPo> levelAiInferenceFields = level.stream()
                    .filter(field -> com.lixh.webexample.data.enums.FieldType.AI_INFERENCE.getValue()
                            .equals(field.getType()))
                    .toList();

            if (levelAiInferenceFields.isEmpty()) {
                continue;
            }

            // 检查当前层级是否有未解析的字段
            boolean hasUnparsedFields = false;
            for (FieldConfigPo field : levelAiInferenceFields) {
                ParseDetailPo detail = fieldIdToDetailMap.get(field.getId());
                if (detail == null || detail.getFieldValue() == null || detail.getFieldValue().isEmpty()) {
                    hasUnparsedFields = true;
                    break;
                }
            }

            // 如果当前层级有未解析的字段，返回该层级索引
            if (hasUnparsedFields) {
                return levelIndex;
            }
        }

        // 所有层级都已解析完成
        return fieldTopology.size();
    }

    /**
     * 构建字段拓扑结构
     *
     * @param fieldConfigs 字段配置列表
     * @return 字段拓扑结构，每个元素是一个层级的字段列表
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

    /**
     * 更新解析状态
     *
     * @param parseHistoryId 解析历史ID
     * @param status         解析状态
     * @param errorMessage   错误信息
     */
    private void updateParseStatus(Long parseHistoryId, ParseStatus status, String errorMessage) {
        try {
            ParseHistoryPo parseHistory = new ParseHistoryPo();
            parseHistory.setId(parseHistoryId);
            parseHistory.setParseStatus(status);
            parseHistory.setErrorMessage(errorMessage);
            parseHistory.setUpdateTime(LocalDateTime.now());

            parseHistoryService.updateParseHistory(parseHistory);
            log.info("已更新解析状态为 {} ，历史ID: {}", status, parseHistoryId);
        } catch (Exception e) {
            log.error("更新解析状态出错，历史ID: {}", parseHistoryId, e);
        }
    }

    /**
     * 生成数据库模板
     *
     * @param materialType 材料类型
     * @return 生成的Excel文件字节数组
     */
    public byte[] generateDatabaseTemplate(String materialType) {
        log.info("开始生成数据库模板，材料类型: {}", materialType);

        try {
            // 获取字段配置
            List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(materialType);

            if (fieldConfigs.isEmpty()) {
                throw new BusinessException("未找到字段配置");
            }

            // 过滤掉系统列
            fieldConfigs = fieldConfigs.stream()
                    .filter(field -> !isSystemColumn(field))
                    .collect(Collectors.toList());

            if (fieldConfigs.isEmpty()) {
                throw new BusinessException("过滤系统列后没有可用的字段配置");
            }

            // 创建工作簿
            XSSFWorkbook workbook = new XSSFWorkbook();

            // 找出所有的父子关系对
            Map<Long, List<FieldConfigPo>> childrenByParentId = new HashMap<>();
            List<FieldConfigPo> noRelationFields = new ArrayList<>();

            // 将字段按照父子关系分组
            for (FieldConfigPo field : fieldConfigs) {
                if (field.getParentId() != null && field.getParentId() > 0) {
                    // 有父节点的字段
                    childrenByParentId.computeIfAbsent(field.getParentId(), k -> new ArrayList<>()).add(field);
                } else {
                    // 没有父节点的字段，暂时放入 noRelationFields
                    noRelationFields.add(field);
                }
            }

            // 从 noRelationFields 中移除作为父节点的字段
            noRelationFields.removeIf(field -> childrenByParentId.containsKey(field.getId()));

            // 创建父子关系 Sheet
            for (Map.Entry<Long, List<FieldConfigPo>> entry : childrenByParentId.entrySet()) {
                Long parentId = entry.getKey();
                List<FieldConfigPo> children = entry.getValue();

                // 找出父节点
                FieldConfigPo parent = fieldConfigs.stream()
                        .filter(field -> field.getId().equals(parentId))
                        .findFirst()
                        .orElse(null);

                if (parent == null) {
                    continue;
                }

                // 为每个子节点创建一个 Sheet
                for (FieldConfigPo child : children) {
                    // 创建 Sheet 名称，使用父子节点名称，确保不超过 31 个字符
                    String parentName = parent.getName();
                    String childName = child.getName();

                    // 如果名称太长，进行截断
                    if (parentName.length() + childName.length() > 25) {
                        int maxLength = 12; // 为每个名称保留的最大长度
                        if (parentName.length() > maxLength) {
                            parentName = parentName.substring(0, maxLength) + "...";
                        }
                        if (childName.length() > maxLength) {
                            childName = childName.substring(0, maxLength) + "...";
                        }
                    }

                    String sheetName = parentName + "-" + childName;
                    // 替换不允许在sheet名称中出现的字符
                    sheetName = sheetName.replaceAll("[\\[\\]\\*\\?/\\\\]", "_");

                    XSSFSheet sheet = workbook.createSheet(sheetName);

                    // 设置第一行第一列的字符串
                    XSSFRow row0 = sheet.createRow(0);
                    XSSFCell cell0 = row0.createCell(0);
                    cell0.setCellValue(materialType + ":" + parent.getName() + ":" + child.getName());

                    // 设置第二行的填写说明
                    XSSFRow row1 = sheet.createRow(1);
                    XSSFCell cell1 = row1.createCell(0);
                    cell1.setCellValue("请不要删除本行 - 填写说明：1.从第四行开始填写数据 2.父字段在左，子字段在右 3.保持数据格式一致");

                    // 设置表头（第三行）
                    XSSFRow headerRow = sheet.createRow(2);

                    // 确保父字段在左，子字段在右
                    XSSFCell parentCell = headerRow.createCell(0);
                    parentCell.setCellValue(parent.getName());

                    XSSFCell childCell = headerRow.createCell(1);
                    childCell.setCellValue(child.getName());
                }
            }

            // 创建无父子关系 Sheet
            if (!noRelationFields.isEmpty()) {
                XSSFSheet sheet = workbook.createSheet("NO_RELATION");

                // 设置第一行第一列的字符串
                XSSFRow row0 = sheet.createRow(0);
                XSSFCell cell0 = row0.createCell(0);
                cell0.setCellValue("NO_RELATION");

                // 设置第二行的填写说明
                XSSFRow row1 = sheet.createRow(1);
                XSSFCell cell1 = row1.createCell(0);
                cell1.setCellValue("请不要删除本行 - 填写说明：1.从第四行开始填写数据 2.按照表头顺序填写 3.保持数据格式一致");

                // 设置表头（第三行）
                XSSFRow headerRow = sheet.createRow(2);

                // 将字段按字段名排序
                noRelationFields.sort(Comparator.comparing(FieldConfigPo::getName));

                // 设置表头单元格
                for (int i = 0; i < noRelationFields.size(); i++) {
                    XSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(noRelationFields.get(i).getName());
                }
            }

            // 将工作簿转换为字节数组
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            return bos.toByteArray();
        } catch (Exception e) {
            log.error("生成数据库模板失败", e);
            throw new BusinessException("生成数据库模板失败: " + e.getMessage());
        }
    }

    /**
     * 判断字段是否为系统列
     * 
     * @param field 字段配置
     * @return 是否为系统列
     */
    private boolean isSystemColumn(FieldConfigPo field) {
        // 根据字段名称或类型判断是否为系统列
        // 这里假设系统列的名称包含特定前缀或后缀，或者有特定的类型标识
        return FieldType.fromValue(field.getType()) == FieldType.SYSTEM;
    }

    /**
     * 处理上传的数据库模板
     *
     * @param file         上传的Excel文件
     * @param materialType 材料类型
     * @return 处理结果
     */
    public DatabaseTemplateUploadResponse processDatabaseTemplate(MultipartFile file, String materialType) {
        log.info("开始处理数据库模板，材料类型: {}", materialType);

        DatabaseTemplateUploadResponse response = new DatabaseTemplateUploadResponse();

        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("上传文件不能为空");
            }

            // 获取字段配置
            List<FieldConfigPo> fieldConfigs = fieldConfigService.getFieldConfigs(materialType);

            if (fieldConfigs.isEmpty()) {
                throw new BusinessException("未找到字段配置");
            }

            // 过滤掉系统列
            fieldConfigs = fieldConfigs.stream()
                    .filter(field -> !isSystemColumn(field))
                    .collect(Collectors.toList());

            if (fieldConfigs.isEmpty()) {
                throw new BusinessException("过滤系统列后没有可用的字段配置");
            }

            // 创建字段ID到字段名称的映射
            Map<String, Long> fieldNameToIdMap = fieldConfigs.stream()
                    .collect(Collectors.toMap(FieldConfigPo::getName, FieldConfigPo::getId));

            // 创建批次ID
            String batchId = generateBatchId(materialType);

            // 读取Excel文件
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());

            // 用于存储解析的数据
            List<DatabasePo> databaseRecords = new ArrayList<>();

            // 处理每个Sheet
            int sheetCount = workbook.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                // 读取第一行第一列的关系信息
                XSSFRow row0 = sheet.getRow(0);
                if (row0 == null) {
                    log.warn("Sheet {} 第一行为空，跳过", sheetName);
                    continue;
                }

                XSSFCell cell0 = row0.getCell(0);
                if (cell0 == null) {
                    log.warn("Sheet {} 第一行第一列为空，跳过", sheetName);
                    continue;
                }

                String relationInfo = cell0.getStringCellValue();

                // 解析关系信息
                if ("NO_RELATION".equals(relationInfo)) {
                    // 处理无关系的Sheet
                    processNoRelationSheet(sheet, fieldNameToIdMap, materialType, batchId, databaseRecords);
                } else {
                    // 处理父子关系的Sheet
                    processParentChildSheet(sheet, relationInfo, fieldNameToIdMap, materialType, batchId,
                            databaseRecords);
                }
            }

            // 关闭工作簿
            workbook.close();

            // 保存数据到数据库
            int importCount = 0;
            if (!databaseRecords.isEmpty()) {
                // 批量保存
                importCount = databaseService.batchSave(databaseRecords);
            }

            // 设置响应
            response.setSuccess(true);
            response.setMessage("数据库模板处理成功");
            response.setImportCount(importCount);

            return response;
        } catch (Exception e) {
            log.error("处理数据库模板失败", e);
            response.setSuccess(false);
            response.setMessage("处理数据库模板失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 处理无关系的Sheet
     *
     * @param sheet            Sheet对象
     * @param fieldNameToIdMap 字段名称到ID的映射
     * @param materialType     材料类型
     * @param batchId          批次ID
     * @param databaseRecords  数据库记录列表
     */
    private void processNoRelationSheet(XSSFSheet sheet, Map<String, Long> fieldNameToIdMap,
            String materialType, String batchId,
            List<DatabasePo> databaseRecords) {
        // 读取表头（第三行）
        XSSFRow headerRow = sheet.getRow(2);
        if (headerRow == null) {
            log.warn("Sheet {} 表头行为空，跳过", sheet.getSheetName());
            return;
        }

        // 获取表头字段名称
        Map<Integer, String> columnIndexToFieldName = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            XSSFCell cell = headerRow.getCell(i);
            if (cell != null) {
                String fieldName = cell.getStringCellValue();
                if (fieldName != null && !fieldName.isEmpty()) {
                    columnIndexToFieldName.put(i, fieldName);
                }
            }
        }

        // 从第四行开始读取数据
        for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            XSSFRow dataRow = sheet.getRow(rowIndex);
            if (dataRow == null) {
                continue;
            }

            // 检查行是否有数据
            boolean hasData = false;
            for (int i = 0; i < dataRow.getLastCellNum(); i++) {
                XSSFCell cell = dataRow.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }
            }

            if (!hasData) {
                continue;
            }

            // 读取每一列的数据
            for (Map.Entry<Integer, String> entry : columnIndexToFieldName.entrySet()) {
                int columnIndex = entry.getKey();
                String fieldName = entry.getValue();

                XSSFCell cell = dataRow.getCell(columnIndex);
                if (cell == null) {
                    continue;
                }

                String cellValue = getCellValueAsString(cell);
                if (cellValue == null || cellValue.isEmpty()) {
                    continue;
                }

                // 设置字段ID和值
                Long fieldId = fieldNameToIdMap.get(fieldName);
                if (fieldId != null) {
                    // 创建数据库记录
                    DatabasePo record = createDatabaseRecord(
                            materialType,
                            batchId,
                            RelationType.NO_RELATION.getValue(),
                            fieldId,
                            cellValue,
                            null,
                            "");

                    databaseRecords.add(record);
                }
            }
        }
    }

    /**
     * 处理父子关系的Sheet
     *
     * @param sheet            Sheet对象
     * @param relationInfo     关系信息
     * @param fieldNameToIdMap 字段名称到ID的映射
     * @param materialType     材料类型
     * @param batchId          批次ID
     * @param databaseRecords  数据库记录列表
     */
    private void processParentChildSheet(XSSFSheet sheet, String relationInfo,
            Map<String, Long> fieldNameToIdMap,
            String materialType, String batchId,
            List<DatabasePo> databaseRecords) {
        // 解析关系信息
        String[] parts = relationInfo.split(":");
        if (parts.length != 3) {
            log.warn("Sheet {} 关系信息格式不正确: {}", sheet.getSheetName(), relationInfo);
            return;
        }

        String sheetMaterialType = parts[0];
        String parentFieldName = parts[1];
        String childFieldName = parts[2];

        // 验证材料类型
        if (!materialType.equals(sheetMaterialType)) {
            log.warn("Sheet {} 材料类型不匹配: 期望 {}, 实际 {}", sheet.getSheetName(), materialType, sheetMaterialType);
            return;
        }

        // 获取父字段和子字段的ID
        Long parentFieldId = fieldNameToIdMap.get(parentFieldName);
        Long childFieldId = fieldNameToIdMap.get(childFieldName);

        if (parentFieldId == null || childFieldId == null) {
            log.warn("Sheet {} 字段名称不存在: 父字段 {}, 子字段 {}", sheet.getSheetName(), parentFieldName, childFieldName);
            return;
        }

        // 从第四行开始读取数据
        for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            XSSFRow dataRow = sheet.getRow(rowIndex);
            if (dataRow == null) {
                continue;
            }

            // 读取父字段值和子字段值
            XSSFCell parentCell = dataRow.getCell(0);
            XSSFCell childCell = dataRow.getCell(1);

            if (parentCell == null || childCell == null) {
                continue;
            }

            String parentValue = getCellValueAsString(parentCell);
            String childValue = getCellValueAsString(childCell);

            if (parentValue == null || parentValue.isEmpty() || childValue == null || childValue.isEmpty()) {
                continue;
            }

            // 创建数据库记录
            DatabasePo record = createDatabaseRecord(
                    materialType,
                    batchId,
                    RelationType.PARENT_CHILD.getValue(),
                    parentFieldId,
                    parentValue,
                    childFieldId,
                    childValue);

            databaseRecords.add(record);
        }
    }

    /**
     * 获取单元格的值作为字符串
     *
     * @param cell 单元格
     * @return 单元格的值
     */
    private String getCellValueAsString(XSSFCell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // 避免科学计数法
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("获取单元格值失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 确认计算结果
     *
     * @param id 解析历史ID
     * @return 确认结果
     */
    public BaseResponse confirmCalculationResult(Long id) {
        String userName = UserContext.getCurrentUsername();

        // 获取解析历史
        ParseHistoryPo parseHistory = parseHistoryService.getParseHistoryById(id);

        if (parseHistory == null) {
            return BaseResponse.error("解析历史不存在");
        }

        // 检查解析历史的状态
        if (parseHistory.getParseStatus() != ParseStatus.WAITING_FOR_CALCULATION_CONFIRM) {
            return BaseResponse.error("只有等待确认计算结果的任务才能确认");
        }

        try {
            // 在确认计算结果之前，保存AI推算字段到计算列的映射
            boolean mappingSuccess = calculationMappingService.saveAiToCalculationMapping(id);
            if (!mappingSuccess) {
                log.warn("保存AI推算字段到计算列映射失败，ID: {}", id);
                // 这里选择继续处理而不是直接返回错误，因为映射失败不应阻止用户确认计算结果
            } else {
                log.info("成功保存AI推算字段到计算列映射，ID: {}", id);
            }
            
            // 更新解析历史状态为SUCCESS
            parseHistory.setParseStatus(ParseStatus.SUCCESS);
            parseHistory.setConfirmTime(LocalDateTime.now());
            parseHistory.setConfirmedBy(userName);
            parseHistory.setUpdateBy(userName);
            parseHistory.setUpdateTime(LocalDateTime.now());
            
            parseHistoryService.updateParseHistory(parseHistory);
            
            return BaseResponse.success("确认计算结果成功");
        } catch (Exception e) {
            log.error("确认计算结果失败", e);
            return BaseResponse.error("确认计算结果失败: " + e.getMessage());
        }
    }
}
