package com.lixh.webexample.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段配置响应DTO
 */
@Data
public class FieldConfigResponse {

    private Long id;

    private String name;

    private String type;

    private Long parentId;

    private String materialType;

    /**
     * 字段描述
     */
    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Boolean isExisting;
}