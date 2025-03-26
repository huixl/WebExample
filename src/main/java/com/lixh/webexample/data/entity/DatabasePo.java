package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 垫片数据库实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_gasket_database")
public class DatabasePo extends BaseEntity {

    /**
     * 材料类型
     */
    private String materialType;

    /**
     * 父字段ID
     */
    private Long parentFieldId;

    /**
     * 子字段ID
     */
    private Long childFieldId;

    /**
     * 父字段值
     */
    private String parentValue;

    /**
     * 子字段值
     */
    private String childValue;

    /**
     * 关系类型：PARENT_CHILD 或 NO_RELATION
     */
    private String relationType;

    /**
     * 批次ID，用于标识同一次导入的数据
     */
    private String batchId;
}