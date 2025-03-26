package com.lixh.webexample.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 确认解析结果请求DTO
 */
@Data
public class ConfirmResultRequest {

    private List<ConfirmedFieldDTO> confirmedFields;

    private String comment;

    /**
     * 确认字段DTO
     */
    @Data
    public static class ConfirmedFieldDTO {

        private String fieldName;

        private String fieldValue;

        private Long fieldConfigId;

        private Integer rowNum;
    }
}