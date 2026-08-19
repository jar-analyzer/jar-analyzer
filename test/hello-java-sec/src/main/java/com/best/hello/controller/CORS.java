package com.best.hello.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api("跨域资源伪造漏洞")
@RestController
@RequestMapping("/vulnapi/cors")
public class CORS {

    /**
     * 跨源资源共享漏洞，利用需要满足两个条件
     * 1. Access-Control-Allow-Origin可控
     * 2. Access-Control-Allow-Credentials为true  -> 允许带上Cookie
     */
    @GetMapping("/vul")
    public String corsVul(HttpServletRequest request, HttpServletResponse response) {
        // origin头可控
        String origin = request.getHeader("origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        return "cors vul";
    }


    @ApiOperation(value = "safe：白名单判断Origin")
    @CrossOrigin(origins = {"127.0.0.1", "http://127.0.0.1", "https://127.0.0.1"})
    @GetMapping("/safe")
    public String corsSafe(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST");
        return "cors safe";
    }
}
