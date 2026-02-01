package com.example.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用属性配置类
 * 演示@ConfigurationProperties的使用
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 应用名称
     */
    private String name = "SpringBoot2 Test Application";

    /**
     * 应用版本
     */
    private String version = "1.0.0";

    /**
     * 是否开启调试模式
     */
    private boolean debug = false;

    /**
     * 安全配置
     */
    private final Security security = new Security();

    /**
     * 安全相关配置
     */
    @Data
    public static class Security {
        /**
         * 是否启用安全功能
         */
        private boolean enabled = true;

        /**
         * 令牌过期时间（秒）
         */
        private long tokenExpirationSec = 3600;
    }
}