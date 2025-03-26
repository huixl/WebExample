package com.lixh.webexample.dto.response;

import com.lixh.webexample.constant.ParseStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 确认解析结果响应DTO
 */
@Data
public class ConfirmResultResponse {

    private Boolean success;

    private String message;

    private ConfirmResultDataDTO data;

    /**
     * 确认结果数据DTO
     */
    @Data
    public static class ConfirmResultDataDTO {

        private Long id;

        private String materialType;

        private ParseStatus parseStatus;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;

        private LocalDateTime confirmTime;

        private String confirmedBy;
    }
}