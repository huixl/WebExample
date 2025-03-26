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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-26T16:56:57+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class OrderDecompositionConverterImpl implements OrderDecompositionConverter {

    @Override
    public FieldConfigResponse toFieldConfigResponse(FieldConfigPo fieldConfig) {
        if ( fieldConfig == null ) {
            return null;
        }

        FieldConfigResponse fieldConfigResponse = new FieldConfigResponse();

        fieldConfigResponse.setId( fieldConfig.getId() );
        fieldConfigResponse.setName( fieldConfig.getName() );
        fieldConfigResponse.setType( fieldConfig.getType() );
        fieldConfigResponse.setParentId( fieldConfig.getParentId() );
        fieldConfigResponse.setMaterialType( fieldConfig.getMaterialType() );
        fieldConfigResponse.setDescription( fieldConfig.getDescription() );
        fieldConfigResponse.setCreateTime( fieldConfig.getCreateTime() );
        fieldConfigResponse.setUpdateTime( fieldConfig.getUpdateTime() );
        fieldConfigResponse.setCreateBy( fieldConfig.getCreateBy() );
        fieldConfigResponse.setUpdateBy( fieldConfig.getUpdateBy() );

        return fieldConfigResponse;
    }

    @Override
    public FieldConfigPo toFieldConfig(FieldConfigRequest request) {
        if ( request == null ) {
            return null;
        }

        FieldConfigPo fieldConfigPo = new FieldConfigPo();

        fieldConfigPo.setId( request.getId() );
        fieldConfigPo.setName( request.getName() );
        fieldConfigPo.setType( request.getType() );
        fieldConfigPo.setParentId( request.getParentId() );
        fieldConfigPo.setMaterialType( request.getMaterialType() );
        fieldConfigPo.setDescription( request.getDescription() );
        fieldConfigPo.setCreateBy( request.getCreateBy() );
        fieldConfigPo.setUpdateBy( request.getUpdateBy() );

        return fieldConfigPo;
    }

    @Override
    public ParseHistoryResponse toParseHistoryResponse(ParseHistoryPo parseHistory) {
        if ( parseHistory == null ) {
            return null;
        }

        ParseHistoryResponse parseHistoryResponse = new ParseHistoryResponse();

        parseHistoryResponse.setId( parseHistory.getId() );
        parseHistoryResponse.setMaterialType( parseHistory.getMaterialType() );
        parseHistoryResponse.setParseStatusEnum( parseHistory.getParseStatusEnum() );
        parseHistoryResponse.setErrorMessage( parseHistory.getErrorMessage() );
        parseHistoryResponse.setCreateTime( parseHistory.getCreateTime() );
        parseHistoryResponse.setUpdateTime( parseHistory.getUpdateTime() );
        parseHistoryResponse.setCreateBy( parseHistory.getCreateBy() );
        parseHistoryResponse.setUpdateBy( parseHistory.getUpdateBy() );
        parseHistoryResponse.setConfirmTime( parseHistory.getConfirmTime() );
        parseHistoryResponse.setConfirmedBy( parseHistory.getConfirmedBy() );
        parseHistoryResponse.setParseMetadataId( parseHistory.getParseMetadataId() );
        parseHistoryResponse.setCustomName( parseHistory.getCustomName() );

        return parseHistoryResponse;
    }

    @Override
    public ParseHistoryResponse.ParseDetailDTO toParseDetailDTO(ParseDetailPo parseDetail) {
        if ( parseDetail == null ) {
            return null;
        }

        ParseHistoryResponse.ParseDetailDTO parseDetailDTO = new ParseHistoryResponse.ParseDetailDTO();

        parseDetailDTO.setId( parseDetail.getId() );
        parseDetailDTO.setParseHistoryId( parseDetail.getParseHistoryId() );
        parseDetailDTO.setFieldName( parseDetail.getFieldName() );
        parseDetailDTO.setFieldValue( parseDetail.getFieldValue() );
        parseDetailDTO.setFieldConfigId( parseDetail.getFieldConfigId() );
        parseDetailDTO.setRowNum( parseDetail.getRowNum() );
        parseDetailDTO.setStatus( parseDetail.getStatus() );
        parseDetailDTO.setCreateTime( parseDetail.getCreateTime() );
        parseDetailDTO.setCreateBy( parseDetail.getCreateBy() );

        return parseDetailDTO;
    }

    @Override
    public List<ParseHistoryResponse.ParseDetailDTO> toParseDetailDTOList(List<ParseDetailPo> parseDetails) {
        if ( parseDetails == null ) {
            return null;
        }

        List<ParseHistoryResponse.ParseDetailDTO> list = new ArrayList<ParseHistoryResponse.ParseDetailDTO>( parseDetails.size() );
        for ( ParseDetailPo parseDetailPo : parseDetails ) {
            list.add( toParseDetailDTO( parseDetailPo ) );
        }

        return list;
    }

    @Override
    public ParseHistoryResponse.FieldMappingDTO toFieldMappingDTO(FieldMappingPo fieldMapping) {
        if ( fieldMapping == null ) {
            return null;
        }

        ParseHistoryResponse.FieldMappingDTO fieldMappingDTO = new ParseHistoryResponse.FieldMappingDTO();

        fieldMappingDTO.setId( fieldMapping.getId() );
        fieldMappingDTO.setParseHistoryId( fieldMapping.getParseHistoryId() );
        fieldMappingDTO.setMetadataId( fieldMapping.getMetadataId() );
        fieldMappingDTO.setExcelPosition( fieldMapping.getExcelPosition() );
        fieldMappingDTO.setMetadataFieldName( fieldMapping.getMetadataFieldName() );
        fieldMappingDTO.setFieldConfigId( fieldMapping.getFieldConfigId() );

        return fieldMappingDTO;
    }

    @Override
    public List<ParseHistoryResponse.FieldMappingDTO> toFieldMappingDTOList(List<FieldMappingPo> fieldMappings) {
        if ( fieldMappings == null ) {
            return null;
        }

        List<ParseHistoryResponse.FieldMappingDTO> list = new ArrayList<ParseHistoryResponse.FieldMappingDTO>( fieldMappings.size() );
        for ( FieldMappingPo fieldMappingPo : fieldMappings ) {
            list.add( toFieldMappingDTO( fieldMappingPo ) );
        }

        return list;
    }

    @Override
    public ParseHistoryPo toParseHistory(ParseSubmitRequest request) {
        if ( request == null ) {
            return null;
        }

        ParseHistoryPo.ParseHistoryPoBuilder parseHistoryPo = ParseHistoryPo.builder();

        parseHistoryPo.materialType( request.getMaterialType() );
        parseHistoryPo.customName( request.getCustomName() );
        parseHistoryPo.parseMetadataId( request.getParseMetadataId() );

        return parseHistoryPo.build();
    }

    @Override
    public ParseDetailPo toParseDetail(UserInputRequest request) {
        if ( request == null ) {
            return null;
        }

        ParseDetailPo parseDetailPo = new ParseDetailPo();

        return parseDetailPo;
    }

    @Override
    public ParseResultResponse.ParsedFieldDTO toParsedFieldDTO(ParseDetailPo parseDetail) {
        if ( parseDetail == null ) {
            return null;
        }

        ParseResultResponse.ParsedFieldDTO parsedFieldDTO = new ParseResultResponse.ParsedFieldDTO();

        parsedFieldDTO.setFieldName( parseDetail.getFieldName() );
        parsedFieldDTO.setFieldValue( parseDetail.getFieldValue() );

        return parsedFieldDTO;
    }

    @Override
    public List<ParseResultResponse.ParsedFieldDTO> toParsedFieldDTOList(List<ParseDetailPo> parseDetails) {
        if ( parseDetails == null ) {
            return null;
        }

        List<ParseResultResponse.ParsedFieldDTO> list = new ArrayList<ParseResultResponse.ParsedFieldDTO>( parseDetails.size() );
        for ( ParseDetailPo parseDetailPo : parseDetails ) {
            list.add( toParsedFieldDTO( parseDetailPo ) );
        }

        return list;
    }

    @Override
    public ParseResultResponse toParseResultResponse(ParseHistoryPo parseHistory) {
        if ( parseHistory == null ) {
            return null;
        }

        ParseResultResponse parseResultResponse = new ParseResultResponse();

        parseResultResponse.setId( parseHistory.getId() );
        parseResultResponse.setMaterialType( parseHistory.getMaterialType() );
        parseResultResponse.setParseStatusEnum( parseHistory.getParseStatusEnum() );
        parseResultResponse.setCreateTime( parseHistory.getCreateTime() );
        parseResultResponse.setUpdateTime( parseHistory.getUpdateTime() );

        return parseResultResponse;
    }

    @Override
    public ConfirmResultResponse.ConfirmResultDataDTO toConfirmResultDataDTO(ParseHistoryPo parseHistory) {
        if ( parseHistory == null ) {
            return null;
        }

        ConfirmResultResponse.ConfirmResultDataDTO confirmResultDataDTO = new ConfirmResultResponse.ConfirmResultDataDTO();

        confirmResultDataDTO.setId( parseHistory.getId() );
        confirmResultDataDTO.setMaterialType( parseHistory.getMaterialType() );
        confirmResultDataDTO.setParseStatusEnum( parseHistory.getParseStatusEnum() );
        confirmResultDataDTO.setCreateTime( parseHistory.getCreateTime() );
        confirmResultDataDTO.setUpdateTime( parseHistory.getUpdateTime() );
        confirmResultDataDTO.setConfirmTime( parseHistory.getConfirmTime() );
        confirmResultDataDTO.setConfirmedBy( parseHistory.getConfirmedBy() );

        return confirmResultDataDTO;
    }
}
