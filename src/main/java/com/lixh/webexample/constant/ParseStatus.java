package com.lixh.webexample.constant;

/**
 * 解析状态枚举
 */
public enum ParseStatus {
    /**
     * 解析中
     */
    PARSING,

    /**
     * 解析成功
     */
    SUCCESS,

    /**
     * 解析失败
     */
    FAILED,

    /**
     * 等待用户输入
     */
    WAITING_FOR_INPUT,
    
    /**
     * 计算中
     */
    CALCULATING,
    
    /**
     * 计算完成，等待用户确认计算结果
     */
    WAITING_FOR_CALCULATION_CONFIRM;

    /**
     * 从字符串值获取枚举
     *
     * @param value 字符串值
     * @return 枚举值
     */
    public static ParseStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        try {
            return ParseStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ParseStatus value: " + value);
        }
    }

    /**
     * 获取枚举名称
     *
     * @return 枚举名称
     */
    public String getValue() {
        return this.name();
    }
}