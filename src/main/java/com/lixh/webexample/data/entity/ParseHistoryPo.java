package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lixh.webexample.constant.ParseStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 解析历史实体类
 */
@Data
@TableName("t_parse_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseHistoryPo {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物料类型
     */
    @TableField("material_type")
    private String materialType;

    /**
     * 解析状态（PARSING-解析中，SUCCESS-解析成功，FAILED-解析失败，WAITING_FOR_INPUT-等待用户输入）
     */
    @TableField("parse_status")
    private ParseStatusEnum parseStatusEnum;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 自定义名称
     */
    @TableField("custom_name")
    private String customName;

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

    /**
     * 确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 确认人
     */
    @TableField("confirmed_by")
    private String confirmedBy;

    /**
     * 解析元数据ID
     */
    @TableField("parse_metadata_id")
    private Long parseMetadataId;
}