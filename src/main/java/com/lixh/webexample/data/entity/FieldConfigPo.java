package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段配置实体类
 */
@Data
@TableName("t_field_config")
public class FieldConfigPo {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字段名称
     */
    @TableField("name")
    private String name;

    /**
     * 字段类型（系统、AI推断、条件填充、自定义）
     */
    @TableField("type")
    private String type;

    /**
     * 父字段ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 物料类型
     */
    @TableField("material_type")
    private String materialType;

    /**
     * 字段描述
     */
    @TableField("description")
    private String description;

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