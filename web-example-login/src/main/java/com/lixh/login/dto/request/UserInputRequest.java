package com.lixh.login.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 用户输入请求DTO
 */
@Data
public class UserInputRequest {

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