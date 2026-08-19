package com.best.hello.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api("接口未授权")
@RestController
@RequestMapping("/vulnapi/unauth")
public class Unauth {

    @GetMapping("/api/info")
    public String vul() {
        Map<String, String> m = new HashMap<>();

        m.put("name", "zhangwei");
        m.put("card", "130684199512173416");

        return JSON.toJSONString(m);
    }

}
