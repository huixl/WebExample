package com.lixh.webexample.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户输入DTO
 */
@Data
public class UserInputDTO {

    private String userInput;

    private List<ConfirmFieldDTO> confirmFields;

    /**
     * 确认字段DTO
     */
    @Data
    public static class ConfirmFieldDTO {

        private String fieldName;

        private String fieldValue;
    }
}