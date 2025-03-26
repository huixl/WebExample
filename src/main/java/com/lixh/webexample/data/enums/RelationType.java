package com.lixh.webexample.data.enums;

import lombok.Getter;

/**
 * 关系类型枚举
 */
@Getter
public enum RelationType {
    
    /**
     * 父子关系
     */
    PARENT_CHILD("PARENT_CHILD", "父子关系"),
    
    /**
     * 无关系
     */
    NO_RELATION("NO_RELATION", "无关系");
    
    private final String value;
    private final String description;
    
    RelationType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return 枚举实例
     */
    public static RelationType fromValue(String value) {
        for (RelationType type : RelationType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
} 