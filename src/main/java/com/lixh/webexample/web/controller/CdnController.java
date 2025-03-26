package com.lixh.webexample.web.controller;

import com.lixh.webexample.config.UserContext;
import com.lixh.webexample.data.entity.UserPo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;

@Controller
public class CdnController {

    @Autowired
    private ResourceLoader resourceLoader;

    @RequestMapping("/workbench/**")
    public ResponseEntity<String> home() throws IOException {
        UserPo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(302).header("Location", "/workbench/login").build();
        }

        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok().body(content);
    }

    @RequestMapping({ "/workbench/login", "/workbench/register" })
    public ResponseEntity<String> login() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok().body(content);
    }

    @RequestMapping("/{path}")
    public ResponseEntity<Void> redirect(@PathVariable String path) {
        return ResponseEntity.status(302).header("Location", "/workbench/" + path).build();
    }
}