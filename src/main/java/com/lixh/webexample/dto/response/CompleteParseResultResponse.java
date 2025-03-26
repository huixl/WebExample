package com.lixh.webexample.dto.response;

import com.lixh.webexample.constant.ParseStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 完成解析结果响应DTO
 */
@Data
public class CompleteParseResultResponse {

    private Boolean success;

    private String message;

    private CompleteParseResultDataDTO data;

    /**
     * 完成解析结果数据DTO
     */
    @Data
    public static class CompleteParseResultDataDTO {

        private Long id;

        private String materialType;

        private ParseStatus parseStatus;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;

        private LocalDateTime confirmTime;

        private String confirmedBy;
    }
} 