package com.best.hello.controller;

import com.best.hello.entity.XSSEntity;
import com.best.hello.mapper.XSSMapper;
import io.swagger.annotations.Api;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Api("CSV注入漏洞")
@RestController
@RequestMapping("/vulnapi/CSVInjection")
public class CSVInjection {
    @Autowired
    private XSSMapper xssMapper;

    @PostMapping("/save")
    public String save(HttpServletRequest request, HttpSession session) {
        String content = request.getParameter("content");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        String user = session.getAttribute("LoginUser").toString();
        xssMapper.add(user, content, date);
        return "success";
    }

    @GetMapping("/getData")
    public List getData() {
        return xssMapper.list();
    }

    @GetMapping("/delete")
    public String delete(int id) {
        xssMapper.deleteFeedById(id);
        return "success";
    }

    @GetMapping("/exportVul")
    public void exportVul(HttpServletResponse response) throws Exception {
        exportCSV(response, false);
    }

    @GetMapping("/exportSafe")
    public void exportSafe(HttpServletResponse response) throws Exception {
        exportCSV(response, true);
    }

    /**
     * 导出 CSV 文件
     */
    private void exportCSV(HttpServletResponse response, boolean safe) throws IOException {
        List<XSSEntity> data = xssMapper.list();

        String fileName = "csv_injection.csv";
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader("ID", "用户名", "内容", "时间"));

        for (XSSEntity x : data) {
            String content = safe ? filterCSVInjection(x.getContent()) : x.getContent();
            csvPrinter.printRecord(x.getId(), x.getUser(), content, x.getDate());
        }
        csvPrinter.flush();
    }

    private String filterCSVInjection(String input) {
        // 定义需要过滤的特殊字符
        String[] forbiddenChars = {"=", "+", "-", "@"};

        // 遍历特殊字符，将其替换为空字符串
        for (String forbiddenChar : forbiddenChars) {
            input = input.replace(forbiddenChar, "");
        }

        return input;
    }


}
