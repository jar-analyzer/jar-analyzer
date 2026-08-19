package com.best.hello.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Pattern;

@Api("DoS")
@RestController
@RequestMapping("/vulnapi/DoS")
public class DoS {
    Logger log = LoggerFactory.getLogger(DoS.class);

    @ApiOperation(value = "vul: 正则表达式拒绝服务攻击(RegexDos)", notes = "正则表达式 \"(a|aa)+\" 存在无限匹配的可能")
    @GetMapping("/redos/vul")
    public String reDosVul(String content) {
        /**
         * 当编写的正则表达式存在缺陷时, 攻击者可以构造特殊的字符串来大量消耗系统资源，造成服务中断或停止。
         * 错误实践:
         * Regex: ([a-z]+)+
         * Regex: (.*[a-z]){n} n >= 10
         *
         * PoC redos/vul?contnet=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab
         */
        boolean match = Pattern.matches("(a|aa)+", content);
        log.info("[vul] RegexDos");
        return String.format("正则匹配：%s", match);
    }


    @ApiOperation(value = "safe: com.google.re2j", notes = "正则表达式引擎采用了贪婪匹配，避免了无限匹配的情况")
    @GetMapping("/redos/safe")
    public String reDosSafe(String content) {
        boolean match = com.google.re2j.Pattern.matches("(a|aa)+", content);
        log.info("[safe] Dos安全正则");
        return String.format("正则匹配：%s", match);
    }


    @GetMapping("/redos/test")
    public String test(String content) {
        boolean match = Pattern.matches("([0-1]+)?$", content);
        return String.format("正则匹配：%s，正则表达式拒绝服务攻击", match);
    }


    @ApiOperation(value = "vul：图片放大拒绝服务", notes = "攻击者可以通过发送大量请求，要求服务器放大图片，从而使服务器资源耗尽")
    @GetMapping("/imagedos/vul")
    public ResponseEntity<byte[]> resizeImageVul(int width, int height) {
        log.info(String.format("[vul] 图片放大拒绝服务：%s %s", width, height));
        return getImageEntity(width, height);
    }

    @ApiOperation("safe：限制图片的最大尺寸")
    @GetMapping("/imagedos/safe")
    public ResponseEntity<byte[]> resizeImageSafe(int width, int height) {
        width = Math.min(width, 200);
        height = Math.min(height, 200);
        return getImageEntity(width, height);
    }

    private ResponseEntity<byte[]> getImageEntity(int width, int height) {
        try {
            Resource resource = new ClassPathResource("static/img/logo.png");
            File file = resource.getFile();
            BufferedImage originalImage = ImageIO.read(new File(file.getAbsolutePath()));

            // 创建缩放后的图像
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            resizedImage.getGraphics().drawImage(originalImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), 0, 0, null);

            // 将缩放后的图像转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}







