package com.lixh.webexample.data.enums;

import lombok.Getter;

/**
 * 字段类型枚举
 * 对应FieldConfigPo中的type字段
 */
@Getter
public enum FieldType {
    /**
     * 系统字段
     */
    SYSTEM("系统"),

    /**
     * AI推断字段
     */
    AI_INFERENCE("AI推断"),

    /**
     * 条件填充字段
     */
    CONDITION_FILL("条件填充"),

    /**
     * 自定义字段
     */
    CUSTOM("自定义"),

    /**
     * 计算列
     */
    CALCULATED("计算列");

    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取枚举
     *
     * @param value 字符串值
     * @return 对应的枚举值，如果没有匹配则返回null
     */
    public static FieldType fromValue(String value) {
        for (FieldType type : FieldType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

}