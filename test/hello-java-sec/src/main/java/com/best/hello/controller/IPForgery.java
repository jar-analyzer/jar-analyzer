package com.best.hello.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * 获取真实IP
 * 1. 没有使用代理的情况下，直接从 getRemoteAddr() 获取目标真实IP
 * 2. 使用nginx等反向代理的情况下，由于在客户端和服务之间增加了中间层，因此服务器无法直接拿到客户端的 IP，这时取 X-Forwarded-For 中第一个IP得到的确实为客户端真实IP
 */
@Api("IP伪造")
@RestController
@RequestMapping("/vulnapi/IPForgery")
public class IPForgery {

    @ApiOperation("safe：RemoteAddr获取真实IP")
    @GetMapping("/remote")
    public static String remoteIP(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return "RemoteAddr: " + ip;
    }

    @ApiOperation("vul: XFF伪造")
    @GetMapping("/vul")
    public static String xffVul(HttpServletRequest request) {
        Map<String, String> m = new HashMap<>();
        String ip = (request.getHeader("X-Forwarded-For") != null) ? request.getHeader("X-Forwarded-For") : request.getRemoteAddr();
        if (Objects.equals(ip, "127.0.0.1")) {
            m.put("message", "success");
            m.put("flag", "fd65cf072a93c93ad52b9f25b341e10b");
        } else {
            m.put("message", "只允许本地IP访问");
        }
        m.put("ip", ip);
        return JSON.toJSONString(m);
    }

    @ApiOperation("safe: 反向代理获取IP")
    @RequestMapping("/proxy")
    public static String proxyIP(HttpServletRequest request) {
        /**
         * nginx配置
         * nginx将访问者的remote_addr传递给X-Real-IP，后端获取X-Real-IP值即$remote_addr
         *
         * location / {
         *     proxy_pass http://localhost:65412;
         *     proxy_set_header Host $host;
         *     proxy_set_header X-Real-IP $remote_addr;
         *     proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         * }
         **/
        String realIP = request.getHeader("X-Real-IP");
        if (realIP != null && !realIP.trim().isEmpty()) {
            System.out.println("X-Real-IP: " + realIP);
            return realIP;
        }

        /**
         * 如果 X-Real-IP 未设置或无效，则尝试获取 X-Forwarded-For
         * 伪造添加xxf时，每个xxf在后面追加，形式：X-Forwarded-For: 客户端ip， 一级代理ip， 二级代理ip
         */
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            // 获取第一个 IP 地址
            System.out.println("X-Forwarded-For: " + forwardedFor);
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

}
