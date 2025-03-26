package com.lixh.webexample.converter;

import com.lixh.webexample.data.entity.FieldConfigPo;
import com.lixh.webexample.data.entity.FieldMappingPo;
import com.lixh.webexample.data.entity.ParseDetailPo;
import com.lixh.webexample.data.entity.ParseHistoryPo;
import com.lixh.webexample.dto.request.FieldConfigRequest;
import com.lixh.webexample.dto.request.ParseSubmitRequest;
import com.lixh.webexample.dto.request.UserInputRequest;
import com.lixh.webexample.dto.response.ConfirmResultResponse;
import com.lixh.webexample.dto.response.FieldConfigResponse;
import com.lixh.webexample.dto.response.ParseHistoryResponse;
import com.lixh.webexample.dto.response.ParseResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 订单拆解模块的MapStruct映射接口
 */
@Mapper(componentModel = "spring")
public interface OrderDecompositionConverter {

    /**
     * 将FieldConfig实体转换为FieldConfigResponse
     *
     * @param fieldConfig 字段配置实体
     * @return 字段配置响应DTO
     */
    FieldConfigResponse toFieldConfigResponse(FieldConfigPo fieldConfig);

    /**
     * 将FieldConfigRequest转换为FieldConfig实体
     *
     * @param request 字段配置请求DTO
     * @return 字段配置实体
     */
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    FieldConfigPo toFieldConfig(FieldConfigRequest request);

    /**
     * 将ParseHistory实体转换为ParseHistoryResponse
     *
     * @param parseHistory 解析历史实体
     * @return 解析历史响应DTO
     */
    @Mapping(target = "details", ignore = true)
    ParseHistoryResponse toParseHistoryResponse(ParseHistoryPo parseHistory);

    /**
     * 将ParseDetail实体转换为ParseDetailDTO
     *
     * @param parseDetail 解析详情实体
     * @return 解析详情DTO
     */
    ParseHistoryResponse.ParseDetailDTO toParseDetailDTO(ParseDetailPo parseDetail);

    /**
     * 将ParseDetail实体列表转换为ParseDetailDTO列表
     *
     * @param parseDetails 解析详情实体列表
     * @return 解析详情DTO列表
     */
    List<ParseHistoryResponse.ParseDetailDTO> toParseDetailDTOList(List<ParseDetailPo> parseDetails);

    /**
     * 将FieldMapping实体转换为FieldMappingDTO
     *
     * @param fieldMapping 字段映射实体
     * @return 字段映射DTO
     */
    @Mapping(target = "fieldName", ignore = true)
    ParseHistoryResponse.FieldMappingDTO toFieldMappingDTO(FieldMappingPo fieldMapping);

    /**
     * 将FieldMapping实体列表转换为FieldMappingDTO列表
     *
     * @param fieldMappings 字段映射实体列表
     * @return 字段映射DTO列表
     */
    List<ParseHistoryResponse.FieldMappingDTO> toFieldMappingDTOList(List<FieldMappingPo> fieldMappings);

    /**
     * 将ParseSubmitRequest转换为ParseHistory实体
     *
     * @param request 解析提交请求DTO
     * @return 解析历史实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parseStatus", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "confirmTime", ignore = true)
    @Mapping(target = "confirmedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ParseHistoryPo toParseHistory(ParseSubmitRequest request);

    /**
     * 将UserInputRequest转换为ParseDetail实体
     *
     * @param request 用户输入请求DTO
     * @return 解析详情实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parseHistoryId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ParseDetailPo toParseDetail(UserInputRequest request);

    /**
     * 将ParseDetail实体转换为ParseResultResponse.ParsedFieldDTO
     *
     * @param parseDetail 解析详情实体
     * @return 解析字段DTO
     */
    @Mapping(source = "fieldName", target = "fieldName")
    @Mapping(source = "fieldValue", target = "fieldValue")
    ParseResultResponse.ParsedFieldDTO toParsedFieldDTO(ParseDetailPo parseDetail);

    /**
     * 将ParseDetail实体列表转换为ParseResultResponse.ParsedFieldDTO列表
     *
     * @param parseDetails 解析详情实体列表
     * @return 解析字段DTO列表
     */
    List<ParseResultResponse.ParsedFieldDTO> toParsedFieldDTOList(List<ParseDetailPo> parseDetails);

    /**
     * 将ParseHistory实体转换为ParseResultResponse
     *
     * @param parseHistory 解析历史实体
     * @return 解析结果响应DTO
     */
    @Mapping(target = "result", ignore = true)
    ParseResultResponse toParseResultResponse(ParseHistoryPo parseHistory);

    /**
     * 将ParseHistory实体转换为ConfirmResultResponse.ConfirmResultDataDTO
     *
     * @param parseHistory 解析历史实体
     * @return 确认结果数据DTO
     */
    ConfirmResultResponse.ConfirmResultDataDTO toConfirmResultDataDTO(ParseHistoryPo parseHistory);

    /**
     * 创建一个成功的ConfirmResultResponse
     *
     * @return 确认结果响应DTO
     */
    @Named("createSuccessConfirmResultResponse")
    default ConfirmResultResponse createSuccessConfirmResultResponse() {
        ConfirmResultResponse response = new ConfirmResultResponse();
        response.setSuccess(true);
        response.setMessage("解析结果确认成功");
        return response;
    }
}