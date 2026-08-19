package com.best.hello.controller.XSS;

import com.best.hello.mapper.XSSMapper;
import com.best.hello.util.Security;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.owasp.esapi.ESAPI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XSS漏洞
 *
 * @author nul1
 * @date 2021/6/29
 */

@Api("XSS漏洞")
@RestController
@RequestMapping("/vulnapi/XSS")
public class XSS {

    static Logger log = LoggerFactory.getLogger(XSS.class);

    @Autowired
    private XSSMapper xssMapper;

    @ApiOperation(value = "vul: 反射型XSS", notes = "直接返回用户输入内容")
    @GetMapping("/reflect")
    public String xssReflect1(String content) {
        log.info("[vul] 反射型XSS：{}", content);
        return content;
    }


    @ApiOperation(value = "反射型XSS2", notes = "使用HttpServletResponse输出用户输入内容")
    @GetMapping("/reflect2")
    public void xssReflect2(String content, HttpServletResponse response) {
        try {
            // 修复方式设置ContentType类型：response.setContentType("text/plain;charset=utf-8");
            response.getWriter().println(content);
            response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "vul: 存储型XSS", notes = "存储用户输入内容")
    @PostMapping("/save")
    public String save(HttpServletRequest request, HttpSession session) {
        String content = request.getParameter("content");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        String user = session.getAttribute("LoginUser").toString();
        xssMapper.add(user, content, date);
        log.info("[vul] 存储型XSS：{}", content);
        return "success";
    }

    @ApiOperation(value = "获取存储的XSS数据")
    @GetMapping("/getStored")
    public List getStored() {
        return xssMapper.list();
    }

    @ApiOperation(value = "删除存储的XSS数据")
    @GetMapping("/delete")
    public String delete(int id) {
        xssMapper.deleteFeedById(id);
        return "success";
    }

    @ApiOperation(value = "safe: 存储型XSS")
    @PostMapping("/safeSave")
    public String safeSave(HttpServletRequest request, HttpSession session) {
        String content = request.getParameter("content");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        String user = session.getAttribute("LoginUser").toString();

        String safe_content = HtmlUtils.htmlEscape(content);

        xssMapper.add(user, safe_content, date);
        return "success";
    }

    @ApiOperation(value = "safe: 采用实体编码", notes = "采用自带函数HtmlUtils.htmlEscape()来过滤")
    @GetMapping("/escape")
    public String safe1(String content) {
        log.info("[safe] htmlEscape实体编码：{}", content);
        return HtmlUtils.htmlEscape(content);
    }


    @ApiOperation(value = "safe: 过滤特殊字符", notes = "做filterXss方法, 基于转义的方式")
    @GetMapping("/filter")
    public String safe2(String content) {
        log.info("[safe] xss过滤：{}", content);
        return Security.filterXss(content);
    }

    @ApiOperation(value = "safe: 富文本过滤", notes = "采用Jsoup做富文本过滤")
    @GetMapping("/whitelist")
    public String safe3(String content) {
        Safelist whitelist = (new Safelist())
                .addTags("p", "hr", "div", "img", "span", "textarea")  // 设置允许的标签
                .addAttributes("a", "href", "title") // 设置标签允许的属性, 避免如nmouseover属性
                .addProtocols("img", "src", "http", "https")  // img的src属性只允许http和https开头
                .addProtocols("a", "href", "http", "https");
        log.info("[safe] 富文本过滤：{}", content);
        return Jsoup.clean(content, whitelist);
    }

    @ApiOperation(value = "safe: ESAPI")
    @GetMapping("/esapi")
    public String safe4(String content) {
        log.info("[safe] ESAPI：{}", content);
        return ESAPI.encoder().encodeForHTML(content);
    }

    @ApiOperation(value = "safe: OWASP Java Encoder")
    @GetMapping("/owaspEncoder")
    public String safe5(String content) {
        log.info("[safe] Encoder：{}", content);
        return Encode.forHtml(content);
    }

}
