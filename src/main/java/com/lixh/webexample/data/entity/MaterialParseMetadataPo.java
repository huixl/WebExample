package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lixh.webexample.constant.ParseStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 材料解析元数据实体
 */
@Data
@TableName(value = "t_material_parse_metadata", autoResultMap = true)
public class MaterialParseMetadataPo {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 定位字段，JSON格式
     */
    @TableField(typeHandler = com.lixh.webexample.config.JsonbTypeHandler.class)
    private Map<String, String> locationFields;

    /**
     * 字段映射，JSON格式
     */
    @TableField(typeHandler = com.lixh.webexample.config.JsonbTypeHandler.class)
    private Map<String, String> fieldMapping;

    /**
     * 解析状态
     */
    private ParseStatus parseStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}