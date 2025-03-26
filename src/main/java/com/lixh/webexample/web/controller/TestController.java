package com.lixh.webexample.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.lixh.webexample.adapter.BaichuanEmbeddingClient;
import com.lixh.webexample.adapter.dto.EmbeddingResponse;
import com.lixh.webexample.data.entity.MemoryPo;
import com.lixh.webexample.data.entity.MemorySearchPo;
import com.pgvector.PGvector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    BaichuanEmbeddingClient baichuanEmbeddingClient;

    @GetMapping("/try-all.json")
    public Map<String, Object> tryAll() {
        return Map.of("message", "try-all");
    }


}
