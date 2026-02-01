package com.example.test.controller;

import com.example.test.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置属性控制器
 * 演示不同的配置属性访问方式
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private Integer serverPort;

    /**
     * 使用@ConfigurationProperties获取配置
     */
    @GetMapping("/app-properties")
    public AppProperties getAppProperties() {
        return appProperties;
    }

    /**
     * 使用@Value注解获取配置
     */
    @GetMapping("/value-annotation")
    public Map<String, Object> getValueAnnotationProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("applicationName", applicationName);
        properties.put("serverPort", serverPort);
        return properties;
    }

    /**
     * 使用Environment获取配置
     */
    @GetMapping("/environment")
    public Map<String, Object> getEnvironmentProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("applicationName", environment.getProperty("spring.application.name"));
        properties.put("serverPort", environment.getProperty("server.port"));
        properties.put("javaVersion", environment.getProperty("java.version"));
        properties.put("osName", environment.getProperty("os.name"));
        return properties;
    }

    /**
     * 获取所有活动的配置文件
     */
    @GetMapping("/active-profiles")
    public Map<String, Object> getActiveProfiles() {
        Map<String, Object> result = new HashMap<>();
        result.put("activeProfiles", environment.getActiveProfiles());
        result.put("defaultProfiles", environment.getDefaultProfiles());
        return result;
    }
}