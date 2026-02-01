package com.example.test.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 高级路由控制器
 * 演示更复杂的路由配置和请求处理
 */
@RestController
@RequestMapping("/advanced")
public class AdvancedController {

    /**
     * 获取请求头信息
     */
    @GetMapping("/headers")
    public Map<String, Object> getHeaders(@RequestHeader HttpHeaders headers) {
        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        return result;
    }

    /**
     * 获取Cookie信息
     */
    @GetMapping("/cookies")
    public Map<String, Object> getCookies(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        // 获取现有Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            result.put("existingCookies", Arrays.asList(cookies));
        } else {
            result.put("existingCookies", "No cookies found");
        }

        // 设置新Cookie
        Cookie newCookie = new Cookie("testCookie", "cookieValue");
        newCookie.setMaxAge(3600); // 1小时
        newCookie.setPath("/");
        response.addCookie(newCookie);

        result.put("message", "A new cookie 'testCookie' has been set");
        return result;
    }

    /**
     * Session示例
     */
    @GetMapping("/session")
    public Map<String, Object> sessionDemo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 获取会话ID
        result.put("sessionId", session.getId());

        // 获取或设置会话属性
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 1;
        } else {
            visitCount++;
        }
        session.setAttribute("visitCount", visitCount);

        result.put("visitCount", visitCount);
        result.put("message", "您已访问此页面 " + visitCount + " 次");
        result.put("creationTime", session.getCreationTime());
        result.put("lastAccessedTime", session.getLastAccessedTime());

        return result;
    }

    /**
     * 请求参数绑定示例
     */
    @GetMapping("/params")
    public Map<String, Object> paramBinding(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") Integer age,
            @RequestParam(required = false) String[] interests) {

        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("interests", interests);
        return result;
    }

    /**
     * 请求方法匹配示例
     */
    @RequestMapping(value = "/method", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> methodMapping(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("method", request.getMethod());
        result.put("message", "This endpoint supports both GET and POST methods");
        return result;
    }

    /**
     * 请求内容类型匹配示例
     */
    @PostMapping(value = "/content-type", consumes = {"application/json", "application/xml"})
    public Map<String, Object> contentTypeMapping(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Received payload with supported content type");
        result.put("receivedData", payload);
        return result;
    }

    /**
     * 响应内容类型示例
     */
    @GetMapping(value = "/produces", produces = {"application/json"})
    public Map<String, Object> producesMapping() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "This response is in JSON format");
        return result;
    }

    /**
     * 获取所有请求信息
     */
    @GetMapping("/request-info")
    public Map<String, Object> getRequestInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 基本请求信息
        result.put("method", request.getMethod());
        result.put("requestURI", request.getRequestURI());
        result.put("queryString", request.getQueryString());
        result.put("remoteAddr", request.getRemoteAddr());
        result.put("remoteHost", request.getRemoteHost());

        // 请求头
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headersMap.put(headerName, request.getHeader(headerName));
        }
        result.put("headers", headersMap);

        // 请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        result.put("parameters", parameterMap);

        return result;
    }
}