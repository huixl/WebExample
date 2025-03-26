package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.FieldConfigPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 字段配置Mapper接口
 */
@Mapper
public interface FieldConfigMapper extends BaseMapper<FieldConfigPo> {

    /**
     * 根据名称、类型和物料类型查询字段配置
     *
     * @param name         字段名称
     * @param type         字段类型
     * @param materialType 物料类型
     * @return 字段配置
     */
    @Select("SELECT * FROM t_field_config WHERE name = #{name} AND type = #{type} AND material_type = #{materialType} AND deleted = 0")
    FieldConfigPo findByNameAndTypeAndMaterialType(String name, String type, String materialType);

}