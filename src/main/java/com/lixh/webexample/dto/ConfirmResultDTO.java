package com.lixh.webexample.dto;

import lombok.Data;

import java.util.List;

/**
 * 确认解析结果DTO
 */
@Data
public class ConfirmResultDTO {

    private List<ConfirmedFieldDTO> confirmedFields;

    private String comment;

    /**
     * 确认字段DTO
     */
    @Data
    public static class ConfirmedFieldDTO {

        private String fieldName;

        private String fieldValue;
    }
}