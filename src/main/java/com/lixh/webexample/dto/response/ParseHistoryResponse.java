package com.lixh.webexample.dto.response;

import com.lixh.webexample.constant.ParseStatusEnum;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 解析历史响应DTO
 */
@Data
public class ParseHistoryResponse {

    private Long id;

    private String materialType;

    private ParseStatusEnum parseStatusEnum;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private LocalDateTime confirmTime;

    private String confirmedBy;

    private Long parseMetadataId;

    private String customName;

    // 解析详情列表，仅在获取详情时返回
    private List<ParseDetailDTO> details;

    // 字段映射列表，仅在获取详情时返回
    private List<FieldMappingDTO> fieldMappings;

    // 分页相关字段
    private Long total;
    private Integer pageSize;
    private Integer pageNum;

    /**
     * 解析详情DTO
     */
    @Data
    public static class ParseDetailDTO {

        private Long id;

        private Long parseHistoryId;

        private String fieldName;

        private String fieldValue;

        private Long fieldConfigId;

        private Integer rowNum;

        private ParseDetailStatus status;

        private LocalDateTime createTime;

        private String createBy;
    }

    /**
     * 字段映射DTO
     */
    @Data
    public static class FieldMappingDTO {

        private Long id;

        private Long parseHistoryId;

        private Long metadataId;

        private String excelPosition;

        private String metadataFieldName;

        private Long fieldConfigId;

        private String fieldName;
    }
}