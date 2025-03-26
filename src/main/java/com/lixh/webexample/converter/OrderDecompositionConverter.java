package com.lixh.webexample.converter;

import com.lixh.webexample.dto.request.FieldConfigRequest;
import com.lixh.webexample.dto.request.ParseSubmitRequest;
import com.lixh.webexample.dto.request.UserInputRequest;
import com.lixh.webexample.dto.response.FieldConfigResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 订单拆解模块的MapStruct映射接口
 */
@Mapper(componentModel = "spring")
public interface OrderDecompositionConverter {
}