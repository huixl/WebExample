package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.constant.ParseStatusEnum;
import com.lixh.webexample.data.entity.ParseDetailPo;
import com.lixh.webexample.data.entity.ParseHistoryPo;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import com.lixh.webexample.data.mapper.ParseDetailMapper;
import com.lixh.webexample.data.mapper.ParseHistoryMapper;
import com.lixh.webexample.ex.BusinessException;
import com.lixh.webexample.service.ParseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 解析详情Service实现类
 */
@Service
public class ParseDetailServiceImpl extends ServiceImpl<ParseDetailMapper, ParseDetailPo>
        implements ParseDetailService {

    @Autowired
    private ParseDetailMapper parseDetailMapper;

    @Autowired
    private ParseHistoryMapper parseHistoryMapper;

    @Override
    public List<ParseDetailPo> getParseDetails(Long parseHistoryId) {
        return parseDetailMapper.selectList(
                Wrappers.<ParseDetailPo>lambdaQuery()
                        .eq(ParseDetailPo::getParseHistoryId, parseHistoryId));
    }

    @Override
    public List<ParseDetailPo> getParseDetails(Long parseHistoryId, Integer rowNum) {
        return parseDetailMapper.selectList(
                Wrappers.<ParseDetailPo>lambdaQuery()
                        .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                        .eq(ParseDetailPo::getRowNum, rowNum));
    }

    @Override
    public List<ParseDetailPo> getParseDetailsWithPagination(Long parseHistoryId, Integer pageSize, Integer pageNum) {
        return getParseDetailsWithPagination(parseHistoryId, pageSize, pageNum, false);
    }

    @Override
    public List<ParseDetailPo> getParseDetailsWithPagination(Long parseHistoryId, Integer pageSize, Integer pageNum,
            Boolean onlyPending) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 先查询分页后的行号
        List<Integer> pagedRowNums;
        if (!onlyPending) {
            // 不筛选待确认状态，直接查询分页后的行号
            pagedRowNums = parseDetailMapper.selectList(
                    Wrappers.<ParseDetailPo>lambdaQuery()
                            .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                            .select(ParseDetailPo::getRowNum)
                            .groupBy(ParseDetailPo::getRowNum)
                            .orderByAsc(ParseDetailPo::getRowNum)
                            .last("LIMIT " + pageSize + " OFFSET " + offset))
                    .stream()
                    .map(ParseDetailPo::getRowNum)
                    .toList();
        } else {
            // 筛选待确认状态，查询有待确认状态的分页后的行号
            pagedRowNums = parseDetailMapper.selectList(
                    Wrappers.<ParseDetailPo>lambdaQuery()
                            .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                            .eq(ParseDetailPo::getStatus, ParseDetailStatus.PENDING.name())
                            .select(ParseDetailPo::getRowNum)
                            .groupBy(ParseDetailPo::getRowNum)
                            .orderByAsc(ParseDetailPo::getRowNum)
                            .last("LIMIT " + pageSize + " OFFSET " + offset))
                    .stream()
                    .map(ParseDetailPo::getRowNum)
                    .toList();
        }

        // 如果没有行号，返回空列表
        if (pagedRowNums.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 根据分页后的行号查询详情记录
        return parseDetailMapper.selectList(
                Wrappers.<ParseDetailPo>lambdaQuery()
                        .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                        .in(ParseDetailPo::getRowNum, pagedRowNums)
                        .orderByAsc(ParseDetailPo::getRowNum));
    }

    @Override
    public Long countParseDetails(Long parseHistoryId) {
        return countParseDetails(parseHistoryId, false);
    }

    @Override
    public Long countParseDetails(Long parseHistoryId, Boolean onlyPending) {
        if (!onlyPending) {
            // 不筛选待确认状态，直接计算不同行号的数量
            return (long) parseDetailMapper.selectObjs(
                    Wrappers.<ParseDetailPo>lambdaQuery()
                            .select(ParseDetailPo::getRowNum)
                            .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                            .groupBy(ParseDetailPo::getRowNum))
                    .size();
        } else {
            // 筛选待确认状态，计算有待确认状态的不同行号的数量
            return (long) parseDetailMapper.selectObjs(
                    Wrappers.<ParseDetailPo>lambdaQuery()
                            .select(ParseDetailPo::getRowNum)
                            .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                            .eq(ParseDetailPo::getStatus, ParseDetailStatus.PENDING.name())
                            .groupBy(ParseDetailPo::getRowNum))
                    .size();
        }
    }

    @Override
    public ParseDetailPo addParseDetail(ParseDetailPo parseDetail) {
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        parseDetail.setCreateTime(now);
        parseDetail.setUpdateTime(now);
        parseDetail.setDeleted(0);

        // 保存解析详情
        parseDetailMapper.insert(parseDetail);

        return parseDetail;
    }

    @Override
    public ParseDetailPo updateParseDetail(ParseDetailPo parseDetail) {
        // 获取解析历史ID
        Long parseHistoryId = parseDetail.getParseHistoryId();

        // 查询解析历史状态
        ParseHistoryPo parseHistory = parseHistoryMapper.selectById(parseHistoryId);
        if (parseHistory != null && ParseStatusEnum.SUCCESS.equals(parseHistory.getParseStatusEnum())) {
            throw new BusinessException("解析任务已完成，不能再进行修改");
        }

        // 设置更新时间
        parseDetail.setUpdateTime(LocalDateTime.now());

        // 更新解析详情
        parseDetailMapper.updateById(parseDetail);

        return parseDetail;
    }

    @Override
    public boolean deleteParseDetails(Long parseHistoryId) {
        return this.remove(
                Wrappers.<ParseDetailPo>lambdaQuery()
                        .eq(ParseDetailPo::getParseHistoryId, parseHistoryId));
    }

    @Override
    public void batchSave(List<ParseDetailPo> parseDetails) {
        if (parseDetails.isEmpty()) {
            return;
        }

        // 分批处理，每批500条
        int batchSize = 500;
        int totalSize = parseDetails.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalSize);
            List<ParseDetailPo> batch = parseDetails.subList(i, endIndex);

            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            for (ParseDetailPo detail : batch) {
                detail.setCreateTime(now);
                detail.setUpdateTime(now);
                detail.setDeleted(0);

                // 单条插入
                parseDetailMapper.insert(detail);
            }
        }
    }

    @Override
    public int batchUpdateStatus(Long parseHistoryId, ParseDetailStatus status, String updateBy) {
        // 查询解析历史状态
        ParseHistoryPo parseHistory = parseHistoryMapper.selectById(parseHistoryId);
        if (parseHistory != null && ParseStatusEnum.SUCCESS.equals(parseHistory.getParseStatusEnum())) {
            throw new BusinessException("解析任务已完成，不能再进行修改");
        }

        // 批量更新状态
        return parseDetailMapper.batchUpdateStatusByParseHistoryId(parseHistoryId, status, updateBy,
                LocalDateTime.now());
    }

    @Override
    public void batchRemove(List<ParseDetailPo> list) {
        if (list != null && !list.isEmpty()) {
            this.removeByIds(list.stream().map(ParseDetailPo::getId).toList());
        }
    }

    @Override
    public List<ParseDetailPo> getParseDetailsByParseHistoryIdAndFieldConfigId(Long parseHistoryId,
            Long fieldConfigId) {
        return parseDetailMapper.selectList(
                Wrappers.<ParseDetailPo>lambdaQuery()
                        .eq(ParseDetailPo::getParseHistoryId, parseHistoryId)
                        .eq(ParseDetailPo::getFieldConfigId, fieldConfigId)
                        .orderByAsc(ParseDetailPo::getRowNum));
    }

    @Override
    public void batchUpdateParseDetails(List<ParseDetailPo> parseDetails) {
        if (parseDetails != null && !parseDetails.isEmpty()) {
            this.updateBatchById(parseDetails);
        }
    }
}