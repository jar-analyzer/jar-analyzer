package com.best.hello.controller.Deserialization;

import com.best.hello.controller.XXE.Student;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Base64;

/**
 * 反序列化漏洞：序列化是将 Java 对象转换成字节流的过程。而反序列化是将字节流转换成 Java 对象的过程
 * <p>
 * 如何发现漏洞
 * 1. Sink, 使用了序列化和反序列化操作的方法
 * 2. Source, 将用户输入的序列化数据直接反序列化为对象。
 * 3. Sanitizer, 没有对序列化数据进行任何校验或过滤
 * JAVA 常见的序列化和反序列化的方法有JAVA 原生序列化和 JSON 类（fastjson、jackson）序列化
 * <p>
 * 序列化方法
 * 1. JDK(ObjectInputStream.readObject)
 * 2. XMLDecoder.readObject
 * 3. Yaml.load
 * 4. XStream.fromXML
 * 5. ObjectMapper.readValue
 * 6. JSON.parseObject
 * ...
 */

@RestController
@RequestMapping("/vulnapi/Deserialization")
public class Deserialization {
    Logger log = LoggerFactory.getLogger(Deserialization.class);

    /**
     * @poc http://127.0.0.1:8888/Deserialize/readObject/vul?base64=payload
     * payload：java -jar ysoserial-0.0.6-SNAPSHOT-BETA-all.jar CommonsCollections5 "open -a Calculator" | base64
     */
    @ApiOperation(value = "vul：readObject反序列化")
    @RequestMapping("/readObject/vul")
    public String readObject(String base64) {
        try {
            log.info("[vul] 执行反序列化：" + base64);
            base64 = base64.replace(" ", "+");

            byte[] bytes = Base64.getDecoder().decode(base64);

            // 将字节转为输入流
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

            // 反序列化流，将序列化的原始数据恢复为对象
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(stream);
            in.readObject();
            in.close();
            return "反序列化漏洞";
        } catch (Exception e) {
            return "Payload：java -jar ysoserial-0.0.6-SNAPSHOT-BETA-all.jar CommonsCollections5 \"open -a Calculator\" | base64";
        }
    }


    @ApiOperation(value = "safe：反序列化类白/黑名单控制", notes = "Apache Commons IO的ValidatingObjectInputStream来校验反序列化的类")
    @RequestMapping("/readObject/safe")
    public String safe(String base64) {
        try {
            log.info("[safe] 执行反序列化");
            base64 = base64.replace(" ", "+");

            byte[] bytes = Base64.getDecoder().decode(base64);

            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

            // 使用 ValidatingObjectInputStream，只允许反序列化Student class
            ValidatingObjectInputStream ois = new ValidatingObjectInputStream(stream);
            ois.accept(Student.class);
            ois.readObject();
            return "ValidatingObjectInputStream";
        } catch (Exception e) {
            return e.toString();
        }
    }

    @ApiOperation(value = "vul：readUnshared反序列化")
    @RequestMapping("/readUnshared/vul")
    public String readUnshared(String base64) {
        try {
            log.info("[safe] 执行反序列化：" + base64);
            base64 = base64.replace(" ", "+");

            byte[] bytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(stream);
            in.readUnshared();
            in.close();
            return "ObjectInputStream.readUnshared";
        } catch (Exception e) {
            log.warn("[error] readUnshared反序列化失败", e);
            return "ObjectInputStream.readUnshared";
        }
    }


}
