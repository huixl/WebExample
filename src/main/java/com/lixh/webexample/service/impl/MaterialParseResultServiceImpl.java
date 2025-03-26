package com.lixh.webexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lixh.webexample.data.entity.MaterialParseResultPo;
import com.lixh.webexample.data.mapper.MaterialParseResultMapper;
import com.lixh.webexample.service.MaterialParseResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 材料解析结果Service实现类
 */
@Service
public class MaterialParseResultServiceImpl implements MaterialParseResultService {

    @Autowired
    private MaterialParseResultMapper materialParseResultMapper;

    @Override
    public List<MaterialParseResultPo> getByMetadataId(Long metadataId) {
        LambdaQueryWrapper<MaterialParseResultPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaterialParseResultPo::getMetadataId, metadataId)
                .eq(MaterialParseResultPo::getDeleted, 0);
        return materialParseResultMapper.selectList(queryWrapper);
    }
}