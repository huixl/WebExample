package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lixh.webexample.data.entity.ParseHistoryPo;
import com.lixh.webexample.data.mapper.ParseHistoryMapper;
import com.lixh.webexample.service.ParseHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 解析历史Service实现类
 */
@Service
public class ParseHistoryServiceImpl extends ServiceImpl<ParseHistoryMapper, ParseHistoryPo>
        implements ParseHistoryService {

    @Override
    public List<ParseHistoryPo> getParseHistories(String materialType) {
        LambdaQueryWrapper<ParseHistoryPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParseHistoryPo::getMaterialType, materialType);
        return this.list(queryWrapper);
    }

    @Override
    public List<ParseHistoryPo> getParseHistories(String materialType, String keyword, String sortField,
            String sortOrder) {
        LambdaQueryWrapper<ParseHistoryPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParseHistoryPo::getMaterialType, materialType);

        // 添加关键词搜索条件（只模糊搜索自定义名称）
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 确保自定义名称不为空且包含关键词
            queryWrapper.isNotNull(ParseHistoryPo::getCustomName)
                    .ne(ParseHistoryPo::getCustomName, "")
                    .like(ParseHistoryPo::getCustomName, keyword);
        }

        // 添加排序条件
        if (sortField != null && !sortField.trim().isEmpty()) {
            boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);

            switch (sortField) {
                case "id":
                    queryWrapper.orderBy(true, isAsc, ParseHistoryPo::getId);
                    break;
                case "customName":
                    queryWrapper.orderBy(true, isAsc, ParseHistoryPo::getCustomName);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, ParseHistoryPo::getCreateTime);
                    break;
                case "updateTime":
                    queryWrapper.orderBy(true, isAsc, ParseHistoryPo::getUpdateTime);
                    break;
                case "parseStatus":
                    queryWrapper.orderBy(true, isAsc, ParseHistoryPo::getParseStatus);
                    break;
                default:
                    // 默认按ID降序排序
                    queryWrapper.orderByDesc(ParseHistoryPo::getId);
            }
        } else {
            // 默认按ID降序排序
            queryWrapper.orderByDesc(ParseHistoryPo::getId);
        }

        return this.list(queryWrapper);
    }

    @Override
    public ParseHistoryPo getParseHistoryById(Long id) {
        return this.getById(id);
    }

    @Override
    public ParseHistoryPo createParseHistory(ParseHistoryPo parseHistory) {
        this.save(parseHistory);
        return parseHistory;
    }

    @Override
    public ParseHistoryPo updateParseHistory(ParseHistoryPo parseHistory) {
        this.updateById(parseHistory);
        return parseHistory;
    }

    @Override
    public ParseHistoryPo confirmParseHistory(Long id, String confirmedBy) {
        ParseHistoryPo parseHistory = this.getById(id);
        if (parseHistory != null) {
            parseHistory.setConfirmedBy(confirmedBy);
            parseHistory.setConfirmTime(java.time.LocalDateTime.now());
            this.updateById(parseHistory);
        }
        return parseHistory;
    }

    @Override
    public boolean deleteParseHistory(Long id) {
        return this.removeById(id);
    }
}