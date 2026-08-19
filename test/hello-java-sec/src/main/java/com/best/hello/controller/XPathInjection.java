package com.best.hello.controller;

import org.apache.commons.text.StringEscapeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.xml.sax.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import java.io.StringReader;

@Api("XPath 注入")
@RestController
@RequestMapping("/vulnapi/XPath")
public class XPathInjection {

    Logger log = LoggerFactory.getLogger(XPathInjection.class);

    /**
     * XPath 注入是一种特殊的注入攻击，它利用应用程序使用 XPath 查询 XML 文档的特性来实现攻击
     * 输入 admin' or '1'='1
     * XPath语句 /users/user[username='admin' or '1'='1' and password='aa']
     */
    @ApiOperation(value = "vul: xpath 注入")
    @GetMapping("/vul")
    public String vul(@RequestParam("username") String username, @RequestParam("password") String password) {
        try {
            // 构造 XML 文档
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<users>"
                            + "<user>"
                            + "<username>admin</username>"
                            + "<password>abc123123</password>"
                            + "</user>"
                            + "</users>")));

            // 解析 XML 文档
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate("/users/user[username='" + username + "' and password='" + password + "']", doc, XPathConstants.NODESET);

            // 检查查询结果
            if (nodes.getLength() > 0) {
                // 用户名和密码验证通过 :)
                log.info("[vul] xpath注入成功");
                return "用户名和密码验证通过！";
            } else {
                // 用户名和密码验证失败 :(
                log.info("[vul] xpath注入失败");
                return "用户名或密码错误！";
            }
        } catch (Exception e) {
            log.error("[vul] 发生异常：" + e.getMessage(), e);
            return "发生异常：" + e.getMessage();
        }
    }

    @ApiOperation(value = "safe: xpath 转义")
    @GetMapping("/safe")
    public String safe(@RequestParam("username") String username, @RequestParam("password") String password) {
        // StringEscapeUtils.escapeXml10() 是 Apache Commons Lang 库中的一个方法，该方法会将字符串中的 &、<、>、' 和 " 转换为实体引用，以便安全地在 XML 文档中使用这些字符。
        String escapeUsername = StringEscapeUtils.escapeXml10(username);
        String escapePassword = StringEscapeUtils.escapeXml10(password);
        log.info("[vul] xpath 转义：" + escapeUsername + " " + escapePassword);

        try {
            // 构造 XML 文档
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<users>"
                            + "<user>"
                            + "<username>admin</username>"
                            + "<password>abc123123</password>"
                            + "</user>"
                            + "</users>")));

            // 解析 XML 文档
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate("/users/user[username='" + escapeUsername + "' and password='" + escapePassword + "']", doc, XPathConstants.NODESET);

            // 检查查询结果
            if (nodes.getLength() > 0) {
                // 用户名和密码验证通过 :)
                return "用户名和密码验证通过！";
            } else {
                // 用户名和密码验证失败 :(
                return "用户名或密码错误！";
            }
        } catch (Exception e) {
            log.error("[vul] 发生异常：" + e.getMessage(), e);
            return "发生异常：" + e.getMessage();
        }
    }
}
