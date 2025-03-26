package com.lixh.webexample.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {

    @GetMapping("/loginInfo.json")
    Map<String, Object> loginInfo() {
        return Map.of("message", "loginInfo");
    }

}
