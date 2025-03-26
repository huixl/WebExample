package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段映射实体类
 */
@Data
@TableName("t_field_mapping")
public class FieldMappingPo {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 解析历史ID
     */
    @TableField("parse_history_id")
    private Long parseHistoryId;

    /**
     * 元数据ID
     */
    @TableField("metadata_id")
    private Long metadataId;

    /**
     * Excel位置
     */
    @TableField("excel_position")
    private String excelPosition;

    /**
     * 元数据字段名称
     */
    @TableField("metadata_field_name")
    private String metadataFieldName;

    /**
     * 字段配置ID
     */
    @TableField("field_config_id")
    private Long fieldConfigId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleted;
}