package com.best.hello.controller;

import com.best.hello.util.HttpClientUtils;
import com.best.hello.util.Security;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

/**
 * 审计的函数
 * 1. URL
 * 2. HttpClient
 * 3. OkHttpClient
 * 4. HttpURLConnection
 * 5. Socket
 * 6. ImageIO
 * 7. DriverManager.getConnection
 * 8. SimpleDriverDataSource.getConnection
 */

@Api("SSRF")
@RestController
@RequestMapping("/vulnapi/SSRF")
public class SSRF {
    Logger log = LoggerFactory.getLogger(SSRF.class);

    /**
     * @poc http://127.0.0.1:8888/SSRF/URLConnection/vul?url=http://www.baidu.com
     * @poc http://127.0.0.1:8888/SSRF/URLConnection/vul?url=file:///etc/passwd
     */
    @ApiOperation(value = "vul：HTTPURLConnection类")
    @GetMapping("/URLConnection/vul")
    public String URLConnection(String url) {
        log.info("[vul] SSRF：" + url);
        return HttpClientUtils.URLConnection(url);
    }


    /**
     * 短链接绕过：http://127.0.0.1:8888/SSRF/URLConnection/vul2?url=http://surl-8.cn/0
     * ip进制绕过：http://127.0.0.1:8888/SSRF/URLConnection/vul2?url=http://168302434
     */
    @ApiOperation(value = "vul：绕过")
    @GetMapping("/URLConnection/vul2")
    public String URLConnection2(String url) {
        if (!Security.isHttp(url)) {
            return "不允许非http协议!!!";
        } else if (Security.isIntranet(Security.urltoIp(url))) {
            return "不允许访问内网!!!";
        } else {
            return HttpClientUtils.URLConnection(url);
        }
    }


    @ApiOperation(value = "safe：白名单方式")
    @GetMapping("/URLConnection/safe")
    public String URLConnection3(String url) {
        if (!Security.isHttp(url)) {
            return "不允许非http/https协议!!!";
        } else if (!Security.isWhite(url)) {
            return "非可信域名！";
        } else {
            return HttpClientUtils.URLConnection(url);
        }
    }


    @ApiOperation(value = "safe：过滤方式")
    @GetMapping("/HTTPURLConnection/safe")
    public String HTTPURLConnection(String url) {
        // 校验 url 是否以 http 或 https 开头
        if (!Security.isHttp(url)) {
            log.error("[HTTPURLConnection] 非法的 url 协议：" + url);
            return "不允许非http/https协议!!!";
        }

        // 解析 url 为 IP 地址
        String ip = Security.urltoIp(url);
        log.info("[HTTPURLConnection] SSRF解析IP：" + ip);

        // 校验 IP 是否为内网地址
        if (Security.isIntranet(ip)) {
            log.error("[HTTPURLConnection] 不允许访问内网：" + ip);
            return "不允许访问内网!!!";
        }

        // 访问 url
        try {
            return HttpClientUtils.HTTPURLConnection(url);
        } catch (Exception e) {
            log.error("[HTTPURLConnection] 访问失败：" + e.getMessage());
            return "访问失败，请稍后再试!!!";
        }
    }

}
