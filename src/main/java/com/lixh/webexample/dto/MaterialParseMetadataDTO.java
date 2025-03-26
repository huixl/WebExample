package com.lixh.webexample.dto;

import com.lixh.webexample.constant.ParseStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 材料解析元数据DTO
 */
@Data
public class MaterialParseMetadataDTO {

    /**
     * 主键ID
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
     * 定位字段，JSON格式
     */
    private Map<String, String> locationFields;

    /**
     * 字段映射，JSON格式
     */
    private Map<String, String> fieldMapping;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}