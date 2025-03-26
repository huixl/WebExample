package com.lixh.webexample.dto.response;

import com.lixh.webexample.constant.ParseStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 解析结果响应DTO
 */
@Data
public class ParseResultResponse {

    private Long id;

    private String materialType;

    private ParseStatus parseStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private ParseResultDataDTO result;

    /**
     * 解析结果数据DTO
     */
    @Data
    public static class ParseResultDataDTO {

        private List<ParsedFieldDTO> parsedFields;

        private Double confidence;

        private String parseTime;
    }

    /**
     * 解析字段DTO
     */
    @Data
    public static class ParsedFieldDTO {

        private String fieldName;

        private String fieldValue;
    }
}