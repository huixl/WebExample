package com.lixh.webexample.web.controller;

import com.lixh.webexample.dto.MaterialParseMetadataDTO;
import com.lixh.webexample.service.MaterialParseService;
import com.lixh.webexample.web.dto.MaterialParseRequest;
import com.lixh.webexample.web.dto.MaterialParseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 材料解析Controller
 */
@RestController
@RequestMapping("/api/material-parse")
@RequiredArgsConstructor
public class MaterialParseController {

    private final MaterialParseService materialParseService;

    /**
     * 上传并解析Excel文件
     */
    @PostMapping("/upload")
    public ResponseEntity<MaterialParseResponse> uploadAndParse(
            @RequestParam("file") MultipartFile file,
            @RequestParam("request") String requestJson) {

        // 将JSON字符串转换为对象
        MaterialParseRequest request = com.alibaba.fastjson.JSON.parseObject(requestJson, MaterialParseRequest.class);

        MaterialParseResponse response = materialParseService.uploadAndParse(file, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取解析结果
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaterialParseResponse> getParseResult(@PathVariable Long id) {
        MaterialParseResponse response = materialParseService.getParseResult(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取解析元数据列表
     *
     * @return 解析元数据列表
     */
    @GetMapping("/metadata/list")
    public ResponseEntity<List<MaterialParseMetadataDTO>> getParseMetadataList() {
        List<MaterialParseMetadataDTO> list = materialParseService.getParseMetadataList();
        return ResponseEntity.ok(list);
    }

    /**
     * 获取解析结果详情
     *
     * @param metadataId 元数据ID
     * @return 解析结果详情
     */
    @GetMapping("/result/details")
    public ResponseEntity<List<Map<String, Object>>> getParseResultDetails(@RequestParam Long metadataId) {
        List<Map<String, Object>> details = materialParseService.getParseResultDetails(metadataId);
        return ResponseEntity.ok(details);
    }

    /**
     * 删除解析记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParseRecord(@PathVariable Long id) {
        boolean success = materialParseService.deleteParseRecord(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}