package com.lixh.webexample.dto.response;

import com.lixh.webexample.constant.ParseStatusEnum;
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

        private ParseStatusEnum parseStatusEnum;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;

        private LocalDateTime confirmTime;

        private String confirmedBy;
    }
} 