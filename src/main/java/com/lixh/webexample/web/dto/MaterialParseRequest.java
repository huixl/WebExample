package com.lixh.webexample.web.dto;

import lombok.Data;

import java.util.Map;

/**
 * 材料解析请求DTO
 */
@Data
public class MaterialParseRequest {

    /**
     * 字段映射配置
     * 格式如：{"C1": "物料描述", "D1": "规格型号"}
     */
    private Map<String, String> fieldMapping;

    /**
     * Excel定位字段，用于定位表头行
     * 格式如：{"A1": "物料编码", "B1": "物料名称"}
     */
    private Map<String, String> locationFields;
}