package com.lixh.webexample.service;

import com.lixh.webexample.dto.MaterialParseMetadataDTO;
import com.lixh.webexample.web.dto.MaterialParseRequest;
import com.lixh.webexample.web.dto.MaterialParseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 材料解析Service接口
 */
public interface MaterialParseService {

    /**
     * 上传并解析Excel文件
     *
     * @param file    Excel文件
     * @param request 解析请求参数
     * @return 解析结果
     */
    MaterialParseResponse uploadAndParse(MultipartFile file, MaterialParseRequest request);

    /**
     * 根据ID获取解析结果
     *
     * @param id 解析ID
     * @return 解析结果
     */
    MaterialParseResponse getParseResult(Long id);

    /**
     * 获取解析元数据列表
     *
     * @return 解析元数据列表
     */
    List<MaterialParseMetadataDTO> getParseMetadataList();

    /**
     * 获取解析结果详情
     *
     * @param metadataId 元数据ID
     * @return 解析结果详情
     */
    List<Map<String, Object>> getParseResultDetails(Long metadataId);

    /**
     * 删除解析记录
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    boolean deleteParseRecord(Long id);
}