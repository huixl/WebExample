package com.lixh.webexample.dto;

import lombok.Data;

/**
 * 字段配置DTO
 */
@Data
public class FieldConfigDTO {

    private Long id;

    private String name;

    private String type;

    private Long parentId;

    private String materialType;

    private String createBy;

    private String updateBy;
}