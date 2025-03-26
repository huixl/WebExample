package com.lixh.webexample.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lixh.webexample.data.entity.ParseDetailPo;
import com.lixh.webexample.data.enums.ParseDetailStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 解析详情Mapper接口
 */
@Mapper
public interface ParseDetailMapper extends BaseMapper<ParseDetailPo> {

    /**
     * 根据解析历史ID查询解析详情列表
     *
     * @param parseHistoryId 解析历史ID
     * @return 解析详情列表
     */
    @Select("SELECT * FROM t_parse_detail WHERE parse_history_id = #{parseHistoryId} AND deleted = 0")
    List<ParseDetailPo> findByParseHistoryId(Long parseHistoryId);

    /**
     * 更新解析详情状态
     *
     * @param id         解析详情ID
     * @param status     状态
     * @param updateBy   更新人
     * @param updateTime 更新时间
     * @return 影响行数
     */
    @Update("UPDATE t_parse_detail SET status = #{status}, update_time = #{updateTime}, update_by = #{updateBy} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updateBy") String updateBy,
            @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据解析历史ID删除解析详情
     *
     * @param parseHistoryId 解析历史ID
     * @param updateBy       更新人
     * @param updateTime     更新时间
     * @return 影响行数
     */
    @Update("UPDATE t_parse_detail SET deleted = 1, update_time = #{updateTime}, update_by = #{updateBy} WHERE parse_history_id = #{parseHistoryId}")
    int deleteByParseHistoryId(@Param("parseHistoryId") Long parseHistoryId, @Param("updateBy") String updateBy,
            @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据解析历史ID批量更新解析详情状态
     *
     * @param parseHistoryId 解析历史ID
     * @param status         状态
     * @param updateBy       更新人
     * @param updateTime     更新时间
     * @return 影响行数
     */
    @Update("UPDATE t_parse_detail SET status = #{status}, update_time = #{updateTime}, update_by = #{updateBy} WHERE parse_history_id = #{parseHistoryId} AND deleted = 0")
    int batchUpdateStatusByParseHistoryId(@Param("parseHistoryId") Long parseHistoryId,
            @Param("status") ParseDetailStatus status,
            @Param("updateBy") String updateBy,
            @Param("updateTime") LocalDateTime updateTime);
}