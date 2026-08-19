package com.best.hello.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.ServerInfo;

import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/admin")
public class Admin {

    @ApiOperation(value = "查询系统基本信息")
    @GetMapping("/sysinfo")
    @ResponseBody
    public String sysInfo() {
        Map<String, String> m = new HashMap<>();
        m.put("app", "Hello Java SEC");
        m.put("author", "nul1");
        m.put("github", "https://github.com/j3ers3/Hello-Java-Sec");
        m.put("tomcat_version", ServerInfo.getServerInfo());
        m.put("java_version", System.getProperty("java.version"));
        m.put("fastjson_version", JSON.VERSION);
        m.put("springboot_version", SpringBootVersion.getVersion());
        m.put("spring_version", SpringVersion.getVersion());

        return JSON.toJSONString(m);
    }

}
