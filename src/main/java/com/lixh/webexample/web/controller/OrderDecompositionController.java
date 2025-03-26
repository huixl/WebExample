package com.lixh.webexample.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lixh.webexample.biz.OrderDecompositionBiz;
import com.lixh.webexample.dto.request.ConfirmResultRequest;
import com.lixh.webexample.dto.request.ContinueAiInferenceRequest;
import com.lixh.webexample.dto.request.FieldConfigRequest;
import com.lixh.webexample.dto.request.ParseSubmitRequest;
import com.lixh.webexample.dto.response.*;
import com.lixh.webexample.ex.BusinessException;
import com.lixh.webexample.util.DistributedLockUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单拆解Controller
 */
@RestController
@RequestMapping("/api/order-decomposition")
@Slf4j
public class OrderDecompositionController {

    @Autowired
    private OrderDecompositionBiz orderDecompositionBiz;

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    /**
     * 获取字段配置列表
     *
     * @param materialType 物料类型
     * @return 字段配置列表
     */
    @GetMapping("/field-configs")
    public List<FieldConfigResponse> getFieldConfigs(@RequestParam String materialType) {
        return orderDecompositionBiz.getFieldConfigs(materialType);
    }

    /**
     * 添加字段配置
     *
     * @param request 字段配置请求
     * @return 添加结果
     */
    @PostMapping("/field-configs")
    public FieldConfigResponse addFieldConfig(@RequestBody FieldConfigRequest request) {
        // 获取materialType
        String materialType = request.getMaterialType();
        if (materialType == null || materialType.isEmpty()) {
            throw new IllegalArgumentException("物料类型不能为空");
        }

        // 分布式锁的键
        String lockKey = "field_config:" + materialType;
        // 尝试获取分布式锁
        String lockValue = distributedLockUtil.tryLock(lockKey);

        if (lockValue == null) {
            // 获取锁失败，说明有其他请求正在处理相同的materialType
            log.warn("获取分布式锁失败，materialType: {}", materialType);
            throw new BusinessException("系统正在处理相同类型的配置，请稍后重试");
        }

        try {
            // 获取锁成功，执行创建操作
            return orderDecompositionBiz.addFieldConfig(request);
        } finally {
            // 无论成功与否，都要释放锁
            boolean releaseResult = distributedLockUtil.releaseLock(lockKey, lockValue);
            if (!releaseResult) {
                log.error("释放分布式锁失败，materialType: {}, lockValue: {}", materialType, lockValue);
            }
        }
    }

    /**
     * 更新字段配置
     *
     * @param id      字段配置ID
     * @param request 字段配置请求
     * @return 更新结果
     */
    @PutMapping("/field-configs/{id}")
    public FieldConfigResponse updateFieldConfig(@PathVariable Long id, @RequestBody FieldConfigRequest request) {
        return orderDecompositionBiz.updateFieldConfig(id, request);
    }

    /**
     * 删除字段配置
     *
     * @param id 字段配置ID
     * @return 删除结果
     */
    @DeleteMapping("/field-configs/{id}")
    public DeleteFieldConfigResponse deleteFieldConfig(@PathVariable Long id) {
        return orderDecompositionBiz.deleteFieldConfig(id);
    }

    /**
     * 获取解析历史列表
     *
     * @param materialType 材料类型
     * @return 解析历史列表
     */
    @GetMapping("/parse-histories")
    public List<ParseHistoryResponse> getParseHistories(@RequestParam String materialType) {
        return orderDecompositionBiz.getParseHistories(materialType);
    }

    /**
     * 获取解析历史列表（支持排序和搜索）
     *
     * @param materialType 材料类型
     * @param keyword      搜索关键词（可选）
     * @param sortField    排序字段（可选）
     * @param sortOrder    排序方式（可选，asc或desc）
     * @return 解析历史列表
     */
    @GetMapping("/parse-histories/search")
    public List<ParseHistoryResponse> getParseHistoriesWithSort(
            @RequestParam String materialType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return orderDecompositionBiz.getParseHistories(materialType, keyword, sortField, sortOrder);
    }

    /**
     * 获取解析详情
     *
     * @param id 解析历史ID
     * @return 解析详情
     */
    @GetMapping("/parse-histories/{id}/detail")
    public ParseHistoryResponse getParseDetail(@PathVariable Long id) {
        return orderDecompositionBiz.getParseDetail(id);
    }

    /**
     * 获取解析详情（支持分页）
     *
     * @param id          解析历史ID
     * @param pageSize    每页大小
     * @param pageNum     页码
     * @param onlyPending 是否只显示待确认状态的数据
     * @return 解析详情
     */
    @GetMapping("/parse-histories/{id}/detail/page")
    public ParseHistoryResponse getParseDetailWithPagination(
            @PathVariable Long id,
            @RequestParam(defaultValue = "100") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "false") Boolean onlyPending) {
        return orderDecompositionBiz.getParseDetailWithPagination(id, pageSize, pageNum, onlyPending);
    }

    /**
     * 提交解析任务
     *
     * @param request 解析提交请求
     * @return 解析历史
     */
    @PostMapping("/parse")
    public ParseHistoryResponse submitParse(@RequestBody ParseSubmitRequest request) {
        return orderDecompositionBiz.submitParse(request);
    }

    /**
     * 获取解析结果
     *
     * @param id 解析历史ID
     * @return 解析结果
     */
    @GetMapping("/parse-histories/{id}/result")
    public ParseResultResponse getParseResult(@PathVariable Long id) {
        return orderDecompositionBiz.getParseResult(id);
    }

    /**
     * 确认解析结果
     *
     * @param id      解析历史ID
     * @param request 确认结果请求
     * @return 确认结果
     */
    @PostMapping("/parse-histories/{id}/confirm")
    public ConfirmResultResponse confirmParseResult(@PathVariable Long id, @RequestBody ConfirmResultRequest request) {
        return orderDecompositionBiz.confirmParseResult(id, request);
    }

    /**
     * 完成解析结果
     *
     * @param id 解析历史ID
     * @return 完成解析结果
     */
    @PostMapping("/parse-histories/{id}/complete")
    public CompleteParseResultResponse completeParseResult(@PathVariable Long id) {
        return orderDecompositionBiz.completeParseResult(id);
    }

    /**
     * 确认计算结果
     *
     * @param id 解析历史ID
     * @return 确认结果
     */
    @PostMapping("/parse-histories/{id}/confirm-calculation")
    public BaseResponse confirmCalculationResult(@PathVariable Long id) {
        return orderDecompositionBiz.confirmCalculationResult(id);
    }

    /**
     * 删除解析行
     *
     * @param id     解析历史ID
     * @param rowNum 行号
     * @return 删除结果
     */
    @DeleteMapping("/parse-histories/{id}/rows/{rowNum}")
    public BaseResponse deleteParseRow(@PathVariable Long id, @PathVariable Integer rowNum) {
        return orderDecompositionBiz.deleteParseRow(id, rowNum);
    }

    /**
     * 添加解析行
     *
     * @param id      解析历史ID
     * @param request 行数据
     * @return 添加结果
     */
    @PostMapping("/parse-histories/{id}/rows")
    public BaseResponse addParseRow(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return orderDecompositionBiz.addParseRow(id, request);
    }

    /**
     * 删除解析历史
     *
     * @param id 解析历史ID
     * @return 删除结果
     */
    @DeleteMapping("/parse-histories/{id}")
    public BaseResponse deleteParseHistory(@PathVariable Long id) {
        return orderDecompositionBiz.deleteParseHistory(id);
    }

    /**
     * 清空解析结果中的特定列
     *
     * @param id      解析历史ID
     * @param request 包含字段配置ID的请求
     * @return 操作结果
     */
    @PostMapping("/parse-histories/{id}/clear-column")
    public BaseResponse clearParseColumn(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long fieldConfigId = Long.valueOf(request.get("fieldConfigId").toString());
        return orderDecompositionBiz.clearParseColumn(id, fieldConfigId);
    }

    /**
     * 继续使用AI推断
     *
     * @param id      解析历史ID
     * @param request 用户输入请求
     * @return 操作结果
     */
    @PostMapping("/parse-histories/{id}/continue-inference")
    public BaseResponse continueAiInference(@PathVariable Long id, @RequestBody ContinueAiInferenceRequest request) {
        return orderDecompositionBiz.continueAiInference(id, request.getUserInput());
    }

    /**
     * 下载数据库模板
     *
     * @param materialType 材料类型
     */
    @GetMapping("/database-template")
    public void downloadDatabaseTemplate(@RequestParam String materialType, HttpServletResponse response) {
        try {
            // 生成模板文件
            byte[] excelBytes = orderDecompositionBiz.generateDatabaseTemplate(materialType);

            // 设置响应头
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + materialType + "_database_template.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentLength(excelBytes.length);

            // 写入响应流
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("生成数据库模板失败", e);
            throw new BusinessException("生成数据库模板失败: " + e.getMessage());
        }
    }

    /**
     * 上传数据库模板
     *
     * @param file         Excel文件
     * @param materialType 材料类型
     * @return 上传结果
     */
    @PostMapping("/database-template/upload")
    public DatabaseTemplateUploadResponse uploadDatabaseTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("materialType") String materialType) {
        try {
            DatabaseTemplateUploadResponse response = orderDecompositionBiz.processDatabaseTemplate(file, materialType);
            return response;
        } catch (Exception e) {
            log.error("处理数据库模板失败", e);
            DatabaseTemplateUploadResponse response = new DatabaseTemplateUploadResponse();
            response.setSuccess(false);
            response.setMessage("处理数据库模板失败: " + e.getMessage());
            return response;
        }
    }
}