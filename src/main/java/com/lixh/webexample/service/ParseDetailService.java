package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.ParseDetailPo;
import com.lixh.webexample.data.enums.ParseDetailStatus;

import java.util.List;

/**
 * 解析详情Service接口
 */
public interface ParseDetailService {

    List<ParseDetailPo> getParseDetails(Long parseHistoryId);

    /**
     * 获取解析详情列表
     *
     * @param parseHistoryId 解析历史ID
     * @param rowNum
     * @return 解析详情列表
     */
    List<ParseDetailPo> getParseDetails(Long parseHistoryId, Integer rowNum);

    /**
     * 获取解析详情列表（分页）
     *
     * @param parseHistoryId 解析历史ID
     * @param pageSize       每页大小
     * @param pageNum        页码
     * @return 解析详情列表
     */
    List<ParseDetailPo> getParseDetailsWithPagination(Long parseHistoryId, Integer pageSize, Integer pageNum);

    /**
     * 获取解析详情列表（分页，支持筛选待确认状态）
     *
     * @param parseHistoryId 解析历史ID
     * @param pageSize       每页大小
     * @param pageNum        页码
     * @param onlyPending    是否只显示待确认状态的数据
     * @return 解析详情列表
     */
    List<ParseDetailPo> getParseDetailsWithPagination(Long parseHistoryId, Integer pageSize, Integer pageNum,
            Boolean onlyPending);

    /**
     * 获取解析详情总数
     *
     * @param parseHistoryId 解析历史ID
     * @return 解析详情总数
     */
    Long countParseDetails(Long parseHistoryId);

    /**
     * 获取解析详情总数（支持筛选待确认状态）
     *
     * @param parseHistoryId 解析历史ID
     * @param onlyPending    是否只显示待确认状态的数据
     * @return 解析详情总数
     */
    Long countParseDetails(Long parseHistoryId, Boolean onlyPending);

    /**
     * 添加解析详情
     *
     * @param parseDetail 解析详情
     * @return 添加后的解析详情
     */
    ParseDetailPo addParseDetail(ParseDetailPo parseDetail);

    /**
     * 更新解析详情
     *
     * @param parseDetail 解析详情
     * @return 更新后的解析详情
     */
    ParseDetailPo updateParseDetail(ParseDetailPo parseDetail);

    /**
     * 删除解析详情
     *
     * @param parseHistoryId 解析历史ID
     * @return 是否删除成功
     */
    boolean deleteParseDetails(Long parseHistoryId);

    /**
     * 批量保存解析详情
     *
     * @param parseDetails 解析详情列表
     */
    void batchSave(List<ParseDetailPo> parseDetails);

    /**
     * 批量更新解析详情状态
     *
     * @param parseHistoryId 解析历史ID
     * @param status         状态
     * @param updateBy       更新人
     * @return 影响行数
     */
    int batchUpdateStatus(Long parseHistoryId, ParseDetailStatus status, String updateBy);

    /**
     * 批量删除解析详情
     *
     * @param list 解析详情ID列表
     */
    void batchRemove(List<ParseDetailPo> list);

    /**
     * 获取特定解析历史和字段配置的所有解析详情
     *
     * @param parseHistoryId 解析历史ID
     * @param fieldConfigId  字段配置ID
     * @return 解析详情列表
     */
    List<ParseDetailPo> getParseDetailsByParseHistoryIdAndFieldConfigId(Long parseHistoryId, Long fieldConfigId);

    /**
     * 批量更新解析详情
     *
     * @param parseDetails 解析详情列表
     */
    void batchUpdateParseDetails(List<ParseDetailPo> parseDetails);
}