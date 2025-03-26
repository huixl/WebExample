package com.lixh.webexample.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lixh.webexample.constant.ParseStatusEnum;
import com.lixh.webexample.data.entity.*;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import com.lixh.webexample.util.ConcurrencyUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 解析任务服务
 */
@Service
@Slf4j
public class ParseTaskService {

    private final Executor taskExecutor;

    private final FieldMappingService fieldMappingService;

    private final MaterialParseResultService materialParseResultService;

    private final ParseDetailService parseDetailService;

    private final ParseHistoryService parseHistoryService;

    private final ParserService parserService;

    private final FieldConfigService fieldConfigService;

    private final ConcurrencyUtil concurrencyUtil;

    public ParseTaskService(Executor taskExecutor,
                            FieldMappingService fieldMappingService,
                            MaterialParseResultService materialParseResultService,
                            ParseDetailService parseDetailService,
                            ParseHistoryService parseHistoryService,
                            ParserService parserService,
                            FieldConfigService fieldConfigService,
                            ConcurrencyUtil concurrencyUtil) {
        this.taskExecutor = taskExecutor;
        this.fieldMappingService = fieldMappingService;
        this.materialParseResultService = materialParseResultService;
        this.parseDetailService = parseDetailService;
        this.parseHistoryService = parseHistoryService;
        this.parserService = parserService;
        this.fieldConfigService = fieldConfigService;
        this.concurrencyUtil = concurrencyUtil;
    }

    /**
     * 启动解析任务
     *
     * @param parseHistoryId 解析历史ID
     */
    public void startParseTask(Long parseHistoryId) {
        taskExecutor.execute(() -> {
            try {
                Thread.sleep(1000); // 一秒钟后再启动
                processParseTask(parseHistoryId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                log.error("启动解析任务时被中断", e);
            }
        });
    }

    /**
     * 处理解析任务
     *
     * @param parseHistoryId 解析历史ID
     */
    private void processParseTask(Long parseHistoryId) {
        log.info("开始处理解析任务，历史ID: {}", parseHistoryId);
        try {

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

            // 3. 获取元数据ID
            Long metadataId = parseHistory.getParseMetadataId();
            if (metadataId == null) {
                throw new IllegalStateException("元数据ID为空");
            }

            // 4. 根据元数据ID获取解析结果
            List<MaterialParseResultPo> parseResults = materialParseResultService.getByMetadataId(metadataId);
            if (parseResults.isEmpty()) {
                throw new IllegalStateException("未找到解析结果");
            }

            // 5. 根据映射关系处理数据并保存到parse_detail表
            List<ParseDetailPo> parseDetails = new ArrayList<>();

            for (MaterialParseResultPo result : parseResults) {
                try {
                    // 获取内容（JSONB类型）
                    Map<String, Object> content = result.getContent();
                    // 获取行号
                    Integer rowNum = result.getRowNum();

                    // 处理每个字段映射
                    for (FieldMappingPo mapping : fieldMappings) {
                        String metadataFieldName = mapping.getMetadataFieldName();
                        Long fieldConfigId = mapping.getFieldConfigId();

                        // 从内容中获取字段值
                        String fieldValue = null;
                        if (content != null && content.containsKey(metadataFieldName)) {
                            Object value = content.get(metadataFieldName);
                            if (value != null) {
                                fieldValue = value.toString();
                            }
                        }

                        // 获取字段配置
                        FieldConfigPo fieldConfig = fieldConfigService.getFieldConfigById(fieldConfigId);
                        if (fieldConfig == null) {
                            log.warn("未找到字段配置，ID: {}", fieldConfigId);
                            continue;
                        }

                        // 创建解析详情
                        ParseDetailPo parseDetail = new ParseDetailPo();
                        parseDetail.setParseHistoryId(parseHistoryId);
                        parseDetail.setFieldName(fieldConfig.getName()); // 使用字段配置的名称
                        parseDetail.setFieldValue(fieldValue);
                        parseDetail.setFieldConfigId(fieldConfigId);
                        parseDetail.setRowNum(rowNum); // 设置行号
                        parseDetail.setStatus(ParseDetailStatus.CONFIRMED); // 设置状态为已确认
                        parseDetail.setCreateBy(parseHistory.getCreateBy());
                        parseDetail.setUpdateBy(parseHistory.getUpdateBy());

                        parseDetails.add(parseDetail);
                    }
                } catch (Exception e) {
                    log.error("处理解析结果出错: {}", result.getId(), e);
                    // 继续处理下一个结果
                }
            }

            // 6. 批量保存解析详情
            if (!parseDetails.isEmpty()) {
                parseDetailService.batchSave(parseDetails);
                log.info("已保存 {} 条解析详情，历史ID: {}", parseDetails.size(), parseHistoryId);
            } else {
                throw new IllegalStateException("未生成解析详情");
            }

            // 7. 根据解析状态更新 ParseStatus
            ParseStatusEnum parseStatusEnum = parseUsingAI(parseHistoryId);
            updateParseStatus(parseHistoryId, parseStatusEnum, null);

        } catch (IllegalStateException e) {
            log.error("解析任务验证错误，历史ID {}: {}", parseHistoryId, e.getMessage());
            updateParseStatus(parseHistoryId, ParseStatusEnum.FAILED, e.getMessage());

        } catch (Exception e) {
            log.error("处理解析任务出错，历史ID: {}", parseHistoryId, e);
            updateParseStatus(parseHistoryId, ParseStatusEnum.FAILED, e.getMessage());

        }
    }

    private ParseStatusEnum parseUsingAI(Long parseHistoryId) {
        try {
            // 1. 获取解析详情以获取所有行号
            List<ParseDetailPo> parseDetails = parseDetailService.getParseDetails(parseHistoryId);
            if (parseDetails.isEmpty()) {
                throw new IllegalStateException("未找到解析详情");
            }

            // 2. 获取所有行号
            List<Integer> rowNums = parseDetails.stream()
                    .map(ParseDetailPo::getRowNum)
                    .distinct()
                    .collect(Collectors.toList());

            // 3. 使用并发工具类处理每一行数据
            concurrencyUtil.executeWithConcurrencyControl(
                    rowNums,
                    rowNum -> {
                        try {
                            // 解析一行数据
                            Map<String, String> result = parserService.parseRow(parseHistoryId, rowNum);
                            log.info("行 {} 解析结果: {}", rowNum, result);
                        } catch (Exception e) {
                            log.error("解析行 {} 出错: {}", rowNum, e.getMessage(), e);
                        }
                    },
                    ConcurrencyUtil.ConcurrencyType.AI_INFERENCE
            );

            // 返回等待用户输入状态
            return ParseStatusEnum.WAITING_FOR_INPUT;
        } catch (Exception e) {
            log.error("AI解析出错: {}", e.getMessage(), e);
            // 更新解析状态为失败
            updateParseStatus(parseHistoryId, ParseStatusEnum.FAILED, e.getMessage());
            return ParseStatusEnum.FAILED;
        }
    }

    /**
     * 更新解析状态
     *
     * @param parseHistoryId 解析历史ID
     * @param status         状态
     * @param errorMessage   错误信息
     */
    private void updateParseStatus(Long parseHistoryId, ParseStatusEnum status, String errorMessage) {
        try {
            ParseHistoryPo parseHistory = new ParseHistoryPo();
            parseHistory.setId(parseHistoryId);
            parseHistory.setParseStatusEnum(status);
            parseHistory.setErrorMessage(errorMessage);
            parseHistory.setUpdateTime(LocalDateTime.now());

            parseHistoryService.updateParseHistory(parseHistory);
            log.info("已更新解析状态为 {} ，历史ID: {}", status, parseHistoryId);
        } catch (Exception e) {
            log.error("更新解析状态出错，历史ID: {}", parseHistoryId, e);
        }
    }

}