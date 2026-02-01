package com.example.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 主页控制器
 * 处理根路径请求
 */
@Controller
public class HomeController {

    /**
     * 应用首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}