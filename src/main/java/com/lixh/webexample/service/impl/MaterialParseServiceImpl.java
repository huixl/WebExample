package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lixh.webexample.constant.ParseStatus;
import com.lixh.webexample.data.entity.MaterialParseMetadataPo;
import com.lixh.webexample.data.entity.MaterialParseResultPo;
import com.lixh.webexample.data.mapper.MaterialParseMetadataMapper;
import com.lixh.webexample.data.mapper.MaterialParseResultMapper;
import com.lixh.webexample.dto.MaterialParseMetadataDTO;
import com.lixh.webexample.service.MaterialParseService;
import com.lixh.webexample.web.dto.MaterialParseRequest;
import com.lixh.webexample.web.dto.MaterialParseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 材料解析Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialParseServiceImpl implements MaterialParseService {

    private final MaterialParseMetadataMapper metadataMapper;

    private final MaterialParseResultMapper resultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialParseResponse uploadAndParse(MultipartFile file, MaterialParseRequest request) {
        // 文件名
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename != null ? originalFilename : "unknown.xlsx";

        // 创建响应对象
        MaterialParseResponse response = new MaterialParseResponse();

        // 创建元数据实体
        MaterialParseMetadataPo metadataEntity = new MaterialParseMetadataPo();
        metadataEntity.setFileName(fileName);

        // 设置定位字段和字段映射
        metadataEntity.setLocationFields(request.getLocationFields());
        metadataEntity.setFieldMapping(request.getFieldMapping());

        metadataEntity.setCreateTime(LocalDateTime.now());
        metadataEntity.setUpdateTime(LocalDateTime.now());

        try {
            // 保存文件
            String uploadDir = "uploads/excel";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("无法创建目录: " + uploadDir);
            }

            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            String filePath = uploadDir + File.separator + uniqueFileName;
            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path);

            // 设置文件路径
            metadataEntity.setFilePath(filePath);

            // 解析Excel
            List<Map<String, Object>> parseResult = parseExcel(path.toFile(), request);

            // 设置解析状态
            metadataEntity.setParseStatus(ParseStatus.SUCCESS); // 解析成功

            // 保存元数据到数据库
            metadataMapper.insert(metadataEntity);

            // 保存解析结果到数据库
            saveParseResults(metadataEntity.getId(), parseResult);

            // 设置响应
            response.setId(metadataEntity.getId());
            response.setFileName(fileName);
            response.setParseStatus(ParseStatus.SUCCESS);
            response.setParseResult(parseResult);
            response.setFieldMapping(request.getFieldMapping());

        } catch (Exception e) {
            log.error("解析Excel失败", e);

            // 设置错误信息
            metadataEntity.setParseStatus(ParseStatus.FAILED); // 解析失败
            metadataEntity.setErrorMessage(e.getMessage());

            // 保存到数据库
            metadataMapper.insert(metadataEntity);

            // 设置响应
            response.setId(metadataEntity.getId());
            response.setFileName(metadataEntity.getFileName());
            response.setParseStatus(ParseStatus.FAILED);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public MaterialParseResponse getParseResult(Long id) {
        MaterialParseMetadataPo metadataEntity = metadataMapper.selectById(id);
        if (metadataEntity == null) {
            return null;
        }

        MaterialParseResponse response = new MaterialParseResponse();
        response.setId(metadataEntity.getId());
        response.setFileName(metadataEntity.getFileName());
        response.setParseStatus(metadataEntity.getParseStatus());
        response.setErrorMessage(metadataEntity.getErrorMessage());
        response.setFieldMapping(metadataEntity.getFieldMapping());

        // 获取解析结果
        List<Map<String, Object>> parseResult = getParseResultDetails(id);
        response.setParseResult(parseResult);

        return response;
    }

    @Override
    public List<MaterialParseMetadataDTO> getParseMetadataList() {
        LambdaQueryWrapper<MaterialParseMetadataPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MaterialParseMetadataPo::getCreateTime);

        List<MaterialParseMetadataPo> entities = metadataMapper.selectList(wrapper);

        return entities.stream().map(this::convertToMetadataDTO).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getParseResultDetails(Long metadataId) {
        LambdaQueryWrapper<MaterialParseResultPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialParseResultPo::getMetadataId, metadataId);
        wrapper.orderByAsc(MaterialParseResultPo::getRowNum);

        List<MaterialParseResultPo> entities = resultMapper.selectList(wrapper);

        return entities.stream()
                .map(MaterialParseResultPo::getContent)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteParseRecord(Long id) {
        // 查询元数据
        MaterialParseMetadataPo metadataEntity = metadataMapper.selectById(id);
        if (metadataEntity != null && StringUtils.hasText(metadataEntity.getFilePath())) {
            // 删除文件
            try {
                Files.deleteIfExists(Paths.get(metadataEntity.getFilePath()));
            } catch (IOException e) {
                log.error("删除文件失败: {}", metadataEntity.getFilePath(), e);
            }
        }

        // 删除解析结果
        LambdaQueryWrapper<MaterialParseResultPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialParseResultPo::getMetadataId, id);
        resultMapper.delete(wrapper);

        // 删除元数据
        return metadataMapper.deleteById(id) > 0;
    }

    /**
     * 使用Apache POI解析Excel文件
     *
     * @param file    Excel文件
     * @param request 解析请求
     * @return 解析结果
     */
    private List<Map<String, Object>> parseExcel(File file, MaterialParseRequest request) {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 查找表头行
            int headerRowIndex = -1;
            Map<Integer, String> headerMap = new HashMap<>();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                if (isHeaderRow(row, request.getLocationFields())) {
                    headerRowIndex = i;

                    // 记录表头映射
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            for (Map.Entry<String, String> mapping : request.getFieldMapping().entrySet()) {
                                if (cellValue.equals(mapping.getValue())) {
                                    headerMap.put(j, mapping.getValue());
                                }
                            }
                        }
                    }
                    break;
                }
            }

            // 如果找到表头，处理数据行
            if (headerRowIndex >= 0) {
                for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    Map<String, Object> rowData = new LinkedHashMap<>();
                    rowData.put("rowNum", i + 1); // 行号从1开始

                    // 根据字段映射提取数据
                    boolean hasNonEmptyValue = false;
                    for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
                        int columnIndex = entry.getKey();
                        String fieldName = entry.getValue();

                        Cell cell = row.getCell(columnIndex);
                        if (cell != null) {
                            Object value = getCellValue(cell);
                            if (value != null && !(value instanceof String && ((String) value).trim().isEmpty())) {
                                hasNonEmptyValue = true;
                            }
                            rowData.put(fieldName, value);
                        }
                    }

                    // 只添加非空行
                    if (hasNonEmptyValue) {
                        result.add(rowData);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析Excel失败", e);
            throw new RuntimeException("解析Excel失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 检查当前行是否是表头行
     *
     * @param row            当前行
     * @param locationFields 定位字段
     * @return 是否是表头行
     */
    private boolean isHeaderRow(Row row, Map<String, String> locationFields) {
        if (locationFields == null || locationFields.isEmpty()) {
            return false;
        }

        int matchCount = 0;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String cellValue = getCellValueAsString(cell);
                for (String expectedValue : locationFields.values()) {
                    if (cellValue.equals(expectedValue)) {
                        matchCount++;
                        break;
                    }
                }
            }
        }

        // 如果匹配的字段数量等于定位字段的数量，则认为是表头行
        return matchCount >= locationFields.size();
    }

    /**
     * 获取单元格的值（字符串形式）
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

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
                try {
                    return String.valueOf(cell.getStringCellValue());
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    /**
     * 获取单元格的值（保留原始类型）
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double value = cell.getNumericCellValue();
                    // 如果是整数，返回整数
                    if (value == Math.floor(value)) {
                        return (long) value;
                    }
                    return value;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        double value = cell.getNumericCellValue();
                        if (value == Math.floor(value)) {
                            return (long) value;
                        }
                        return value;
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return null;
        }
    }

    /**
     * 保存解析结果到数据库
     *
     * @param metadataId  元数据ID
     * @param parseResult 解析结果
     */
    private void saveParseResults(Long metadataId, List<Map<String, Object>> parseResult) {
        if (parseResult == null || parseResult.isEmpty()) {
            return;
        }

        for (Map<String, Object> rowData : parseResult) {
            MaterialParseResultPo entity = new MaterialParseResultPo();
            entity.setMetadataId(metadataId);
            entity.setRowNum((Integer) rowData.get("rowNum"));
            entity.setContent(rowData);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            resultMapper.insert(entity);
        }
    }

    /**
     * 将元数据实体转换为DTO
     *
     * @param entity 元数据实体
     * @return 元数据DTO
     */
    private MaterialParseMetadataDTO convertToMetadataDTO(MaterialParseMetadataPo entity) {
        MaterialParseMetadataDTO dto = new MaterialParseMetadataDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}