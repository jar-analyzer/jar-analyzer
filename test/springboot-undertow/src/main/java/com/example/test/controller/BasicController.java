package com.example.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 基本路由控制器
 * 演示基本的GET请求处理
 */
@Controller
@RequestMapping("/basic")
public class BasicController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/";
    }

    /**
     * 简单的GET请求
     */
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello from SpringBoot2!";
    }

    /**
     * 带参数的GET请求
     */
    @GetMapping("/param")
    @ResponseBody
    public Map<String, Object> withParam(@RequestParam(value = "name", defaultValue = "Guest") String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello, " + name + "!");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 路径变量示例
     */
    @GetMapping("/path/{id}")
    @ResponseBody
    public Map<String, Object> withPathVariable(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("message", "Received path variable: " + id);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 返回视图示例
     */
    @GetMapping("/view")
    public String view(Model model) {
        model.addAttribute("message", "这是一个Thymeleaf视图示例");
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "basic-view";
    }
}