package com.lixh.permission.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lixionghui
 */
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    /**
     * 测试
     * @return
     */
    @GetMapping("/test")
    public ResponseEntity<Boolean> test() {
        System.out.println("-----");
        System.out.println("join");
        System.out.println("-----");
        return ResponseEntity.ok(Boolean.TRUE);
    }

}
