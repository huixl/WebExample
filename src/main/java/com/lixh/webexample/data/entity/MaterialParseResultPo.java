package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 材料解析结果实体
 */
@Data
@TableName(value = "t_material_parse_result", autoResultMap = true)
public class MaterialParseResultPo {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 元数据ID
     */
    private Long metadataId;

    /**
     * 行号
     */
    private Integer rowNum;

    /**
     * 行内容，JSON格式
     */
    @TableField(typeHandler = com.lixh.webexample.config.JsonbObjectTypeHandler.class)
    private Map<String, Object> content;

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