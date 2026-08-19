package com.best.hello.controller.ComponentsVul;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
 * Fastjson 是一个 Java 库，可以将 Java 对象转换为 JSON 格式，当然它也可以将 JSON 字符串转换为 Java 对象
 * Github：https://github.com/alibaba/fastjson/wiki/Quick-Start-CN
 *
 */
@Api("Fastjson反序列化漏洞")
@Slf4j
@RestController
@RequestMapping("/vulnapi/Fastjson")
public class FastjsonVul {

    @RequestMapping(value = "/vul", method = {RequestMethod.POST})
    public String vul(@RequestBody String content) {
        try {
            Object obj = JSON.parse(content);
            return obj.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    @ApiOperation(value = "safe: safeMode")
    @RequestMapping(value = "/safeMode", method = {RequestMethod.POST})
    public String safeMode(@RequestBody String content) {
        try {
            /*
             开启safeMode特性，（这里低版本就注释了）
             ParserConfig.getGlobalInstance().setSafeMode(true);
             Object obj = JSON.parse(content);
            */
            return "safeMode";
        } catch (Exception e) {
            return e.toString();
        }
    }

}
