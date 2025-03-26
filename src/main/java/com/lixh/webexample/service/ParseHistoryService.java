package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.ParseHistoryPo;

import java.util.List;

/**
 * 解析历史Service接口
 */
public interface ParseHistoryService {

    /**
     * 获取解析历史列表
     *
     * @param materialType 材料类型
     * @return 解析历史列表
     */
    List<ParseHistoryPo> getParseHistories(String materialType);

    /**
     * 获取解析历史列表
     *
     * @param materialType 材料类型
     * @param keyword      搜索关键词（可选）
     * @param sortField    排序字段（可选）
     * @param sortOrder    排序方式（可选，asc或desc）
     * @return 解析历史列表
     */
    List<ParseHistoryPo> getParseHistories(String materialType, String keyword, String sortField, String sortOrder);

    /**
     * 根据ID获取解析历史
     *
     * @param id 解析历史ID
     * @return 解析历史
     */
    ParseHistoryPo getParseHistoryById(Long id);

    /**
     * 创建解析历史
     *
     * @param parseHistory 解析历史
     * @return 创建后的解析历史
     */
    ParseHistoryPo createParseHistory(ParseHistoryPo parseHistory);

    /**
     * 更新解析历史
     *
     * @param parseHistory 解析历史
     * @return 更新后的解析历史
     */
    ParseHistoryPo updateParseHistory(ParseHistoryPo parseHistory);

    /**
     * 确认解析历史
     *
     * @param id          解析历史ID
     * @param confirmedBy 确认人
     * @return 确认后的解析历史
     */
    ParseHistoryPo confirmParseHistory(Long id, String confirmedBy);

    /**
     * 删除解析历史
     *
     * @param id 解析历史ID
     * @return 是否删除成功
     */
    boolean deleteParseHistory(Long id);
}