package com.lixh.webexample.service;

import com.lixh.webexample.data.entity.MaterialParseResultPo;

import java.util.List;

/**
 * 材料解析结果服务接口
 */
public interface MaterialParseResultService {

    /**
     * 根据元数据ID获取解析结果
     *
     * @param metadataId 元数据ID
     * @return 解析结果列表
     */
    List<MaterialParseResultPo> getByMetadataId(Long metadataId);
}