package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.FieldMappingPo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 字段映射Mapper接口
 */
@Mapper
public interface FieldMappingMapper extends BaseMapper<FieldMappingPo> {

        /**
         * 根据解析历史ID查询字段映射列表
         *
         * @param parseHistoryId 解析历史ID
         * @return 字段映射列表
         */
        @Select("SELECT * FROM t_field_mapping WHERE parse_history_id = #{parseHistoryId} AND deleted = 0")
        List<FieldMappingPo> findByParseHistoryId(Long parseHistoryId);

        /**
         * 根据元数据ID查询字段映射列表
         *
         * @param metadataId 元数据ID
         * @return 字段映射列表
         */
        @Select("SELECT * FROM t_field_mapping WHERE metadata_id = #{metadataId} AND deleted = 0")
        List<FieldMappingPo> findByMetadataId(Long metadataId);

        /**
         * 插入字段映射
         *
         * @param fieldMapping 字段映射
         * @return 影响行数
         */
        @Insert("INSERT INTO t_field_mapping (parse_history_id, metadata_id, excel_position, metadata_field_name, field_config_id, create_time, update_time, create_by, update_by, deleted) "
                        +
                        "VALUES (#{parseHistoryId}, #{metadataId}, #{excelPosition}, #{metadataFieldName}, #{fieldConfigId}, #{createTime}, #{updateTime}, #{createBy}, #{updateBy}, #{deleted})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(FieldMappingPo fieldMapping);

        /**
         * 批量插入字段映射
         *
         * @param fieldMappings 字段映射列表
         * @return 影响行数
         */
        @Insert("<script>" +
                        "INSERT INTO t_field_mapping (parse_history_id, metadata_id, excel_position, metadata_field_name, field_config_id, create_time, update_time, create_by, update_by, deleted) VALUES "
                        +
                        "<foreach collection='list' item='item' separator=','>" +
                        "(#{item.parseHistoryId}, #{item.metadataId}, #{item.excelPosition}, #{item.metadataFieldName}, #{item.fieldConfigId}, #{item.createTime}, #{item.updateTime}, #{item.createBy}, #{item.updateBy}, #{item.deleted})"
                        +
                        "</foreach>" +
                        "</script>")
        int batchInsert(List<FieldMappingPo> fieldMappings);

        /**
         * 更新字段映射
         *
         * @param fieldMapping 字段映射
         * @return 影响行数
         */
        @Update("UPDATE t_field_mapping SET excel_position = #{excelPosition}, metadata_field_name = #{metadataFieldName}, field_config_id = #{fieldConfigId}, update_time = #{updateTime}, update_by = #{updateBy} WHERE id = #{id}")
        int update(FieldMappingPo fieldMapping);

        /**
         * 删除字段映射
         *
         * @param parseHistoryId 解析历史ID
         * @param updateBy       更新人
         * @return 影响行数
         */
        @Update("UPDATE t_field_mapping SET deleted = 1, update_by = #{updateBy}, update_time = NOW() WHERE parse_history_id = #{parseHistoryId}")
        int deleteByParseHistoryId(Long parseHistoryId, String updateBy);
}