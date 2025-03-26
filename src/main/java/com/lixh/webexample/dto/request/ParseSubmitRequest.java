package com.lixh.webexample.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 解析提交请求DTO
 */
@Data
public class ParseSubmitRequest {

    private Long parseMetadataId;

    private String materialType;

    private List<FieldMappingDTO> fieldMappings;

    private String additionalPrompt;

    private String customName;

    /**
     * 字段映射DTO
     */
    @Data
    public static class FieldMappingDTO {

        private String sourceField;

        private String targetField;
    }
}