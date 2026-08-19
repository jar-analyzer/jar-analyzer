package com.best.hello.controller.RCE;

import groovy.lang.GroovyShell;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/vulnapi/RCE/Groovy")
public class GroovyVul {

    /**
     * @poc http://127.0.0.1:8888/RCE/Groovy/vul?cmd="open -a Calculator".execute()
     */
    @ApiOperation(value = "vul：代码注入 - Groovy类")
    @GetMapping("/vul")
    public void vul(String cmd) {
        GroovyShell shell = new GroovyShell();
        shell.evaluate(cmd);
    }


    @GetMapping("/safe")
    public void safe(String cmd) {
        // 定义一个列表来存储安全的代码，不利于维护
        List<String> safeCodeList = Arrays.asList("\"id\".execute()", "\"whoami\".execute()");

        if (!safeCodeList.contains(cmd)) {
            throw new RuntimeException("unsafe code detected: " + cmd);
        }

        GroovyShell shell = new GroovyShell();
        shell.evaluate(cmd);
    }

}
