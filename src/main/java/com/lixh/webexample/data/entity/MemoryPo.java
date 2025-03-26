package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lixh.webexample.data.handler.PGvectorTypeHandler;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Memories entity.
 */
@TableName(value = "memories", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPo {

    /**
     * Unique identifier for the memory.
     */
    @TableId
    private Integer id;

    /**
     * Creator of the memory.
     */
    private String creator;

    /**
     * Timestamp when the memory was created.
     */
    private LocalDateTime createdAt;

    /**
     * Type of the memory.
     */
    private String memoryType;

    /**
     * Content of the memory.
     */
    private String content;

    /**
     * Vector associated with the memory.
     */
    @TableField(typeHandler = PGvectorTypeHandler.class)
    private PGvector vector; // 使用pgvector库处理vector字段

    /**
     * Type of the creator subject.
     */
    private String creatorSubjectType;

    /**
     * ID of the creator subject.
     */
    private String creatorSubjectId;
}