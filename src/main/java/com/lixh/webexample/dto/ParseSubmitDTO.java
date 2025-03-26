package com.lixh.webexample.dto;

import lombok.Data;

import java.util.List;

/**
 * 解析提交DTO
 */
@Data
public class ParseSubmitDTO {

    private Long parseMetadataId;

    private String materialType;

    private List<FieldMappingDTO> fieldMappings;

    private String additionalPrompt;

    /**
     * 字段映射DTO
     */
    @Data
    public static class FieldMappingDTO {

        private String sourceField;

        private String targetField;
    }
}