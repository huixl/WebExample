package com.lixh.login.dto.request;

import lombok.Data;

/**
 * 字段配置请求DTO
 */
@Data
public class FieldConfigRequest {

    private Long id;

    private String name;

    private String type;

    private Long parentId;

    private String materialType;

    /**
     * 字段描述
     */
    private String description;

    private String createBy;

    private String updateBy;
}