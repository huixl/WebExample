package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计算映射实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_calculation_mapping")
public class CalculationMappingPo extends BaseEntity {

    /**
     * 映射键值，由计算规则生成的UUID
     */
    @TableField("mapping_key")
    private String mappingKey;

    /**
     * 材料类型
     */
    @TableField("material_type")
    private String materialType;

    /**
     * 计算条件JSON
     */
    @TableField("condition_json")
    private String conditionJson;

    /**
     * 计算结果JSON
     */
    @TableField("result_json")
    private String resultJson;
} 