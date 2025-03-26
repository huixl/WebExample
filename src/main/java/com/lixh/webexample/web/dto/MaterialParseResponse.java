package com.lixh.webexample.web.dto;

import com.lixh.webexample.constant.ParseStatus;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 材料解析响应DTO
 */
@Data
public class MaterialParseResponse {

    /**
     * 解析ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 解析状态
     */
    private ParseStatus parseStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 解析结果
     * 格式为：[{行号: 1, 字段1: 值1, 字段2: 值2}, {行号: 2, 字段1: 值1, 字段2: 值2}]
     */
    private List<Map<String, Object>> parseResult;

    /**
     * 字段映射配置
     */
    private Map<String, String> fieldMapping;
}