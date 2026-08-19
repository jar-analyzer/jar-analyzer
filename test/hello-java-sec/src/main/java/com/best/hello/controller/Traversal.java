package com.best.hello.controller;

import com.best.hello.util.Security;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * @date 2021/07/15
 */
@Api("目录遍历")
@RestController
@RequestMapping("/vulnapi/Traversal")
public class Traversal {
    Logger log = LoggerFactory.getLogger(Traversal.class);

    @ApiOperation(value = "vul：任意文件下载")
    @GetMapping("/download")
    public String download(String filename, HttpServletResponse response) {
        Map<String, String> m = new HashMap<>();

        // 下载的文件路径
        String filePath = System.getProperty("user.dir") + "/logs/" + filename;
        log.info("[vul] 任意文件下载：{}", filePath);

        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(Paths.get(filePath)))) {
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.setContentLength((int) Files.size(Paths.get(filePath)));
            response.setContentType("application/octet-stream");

            // 使用 Apache Commons IO 库的工具方法将输入流中的数据拷贝到输出流中
            IOUtils.copy(inputStream, response.getOutputStream());
            log.info("文件 {} 下载成功，路径：{}", filename, filePath);
            m.put("message", "success");
        } catch (IOException e) {
            m.put("message", "未找到文件");
        }
        m.put("filepath", filePath);
        return JSON.toJSONString(m);
    }

    @ApiOperation(value = "vul：任意路径遍历")
    @GetMapping("/list")
    public String fileList(String filename) {
        Map<String, String> m = new HashMap<>();
        String filePath = System.getProperty("user.dir") + "/logs/" + filename;
        log.info("[vul] 任意路径遍历：{}", filePath);
        StringBuilder sb = new StringBuilder();

        File f = new File(filePath);
        File[] fs = f.listFiles();

        if (fs != null) {
            for (File ff : fs) {
                sb.append(ff.getName()).append("<br>");
            }
            return sb.toString();
        }

        m.put("message", "目录不存在");
        m.put("filepath", filePath);
        return JSON.toJSONString(m);
    }

    @ApiOperation(value = "safe：自带的安全方法")
    @GetMapping("/download/safe2")
    public String safe2(String filename) {
        Map<String, String> m = new HashMap<>();
        String filePath = System.getProperty("user.dir") + "/logs/" + filename;
        String filePathSafe = Paths.get(filePath).normalize().toString();
        m.put("message", "使用normalize()方法进行路径规范化");
        m.put("originalFilePath", filePath);
        m.put("safeFilePath", filePathSafe);
        return JSON.toJSONString(m);
    }

    @ApiOperation(value = "safe：黑名单过滤")
    @GetMapping("/download/safe")
    public String safe(String filename) {
        Map<String, String> m = new HashMap<>();
        if (!Security.checkTraversal(filename)) {
            m.put("message", "安全通过");
        } else {
            m.put("message", "检测到非法的文件名");
        }
        m.put("filename", filename);
        return JSON.toJSONString(m);
    }
}

