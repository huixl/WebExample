package com.lixh.webexample.dto.response;

import lombok.Data;

/**
 * 数据库模板上传响应
 */
@Data
public class DatabaseTemplateUploadResponse {
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 导入的数据条数
     */
    private int importCount;
} 