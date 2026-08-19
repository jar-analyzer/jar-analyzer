package com.best.hello.controller.LogicFlaw;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Api("密码重置漏洞")
@Controller
@RequestMapping("/vulnapi/LogicFlaw/PasswordReset")
public class PasswordReset {
    private static final Logger log = LoggerFactory.getLogger(PasswordReset.class);

    @ApiOperation(value = "发送验证码-验证码前端回显")
    @PostMapping(value = "/sendcode", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> resetPassword(@RequestParam("mobile") String mobile, HttpServletRequest request) {
        String authCode = generateRandomCode();

        HttpSession session = request.getSession();
        session.setAttribute("mobile", mobile);
        session.setAttribute("authCode", authCode);
        session.setAttribute("authCodeTimestamp", new Date().getTime());

        // 模拟发送验证码短信
        log.info("验证码前端回显：向手机号 {} 发送验证码: {}", mobile, authCode);

        // 漏洞点：返回验证码到前端
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("mobile", mobile);
        response.put("authCode", authCode);
        return response;
    }

    @ApiOperation(value = "重置密码-验证码前端回显")
    @PostMapping(value = "/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> confirmReset(@RequestParam("code") String code, @RequestParam("newPassword") String newPassword, @RequestParam("mobile") String mobile, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String storedMobile = (String) session.getAttribute("mobile");
        String storedCode = (String) session.getAttribute("authCode");
        long authCodeTimestamp = (Long) session.getAttribute("authCodeTimestamp");
        Map<String, String> response = new HashMap<>();
        Map<String, String> userPasswords = new HashMap<>();

        // 漏洞点：手机号与验证码未进行匹配性验证，修复方式：验证手机号和验证码是否正确
        if (storedMobile.equals(mobile) && storedCode.equals(code)) {
            long currentTime = new Date().getTime();
            long timeDifference = currentTime - authCodeTimestamp;

            // 漏洞点：验证码可破解，修复方式：验证验证码时间戳是否在有效期内（1分钟过期）
            if (timeDifference <= 60000) {
                userPasswords.put(storedMobile, newPassword);
                log.info("密码重置成功，手机号：{}，新密码：{}", storedMobile, newPassword);
                response.put("message", "密码重置成功！");
                response.put("flag", "528277b84feab3feb7a2733f09198842");
                response.put("success", "true");
            } else {
                response.put("message", "验证码已过期，请重新获取！");
                response.put("success", "false");
            }
        } else {
            response.put("message", "验证码错误！");
            response.put("success", "false");
        }
        return response;
    }


    @ApiOperation(value = "发送验证码-验证码可爆破")
    @PostMapping(value = "/sendcode2", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> resetPassword2(@RequestParam("mobile") String mobile, HttpServletRequest request) {
        // 4位数验证码更易爆破
        String authCode = generateRandomCode2();

        HttpSession session = request.getSession();
        session.setAttribute("mobile", mobile);
        session.setAttribute("authCode", authCode);
        session.setAttribute("authCodeTimestamp", new Date().getTime());

        log.info("正常发送：向手机号 {} 发送验证码: {}", mobile, authCode);

        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("mobile", mobile);
        return response;
    }

    @ApiOperation(value = "重置密码-验证码可爆破")
    @PostMapping(value = "/reset2", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> confirmReset2(@RequestParam("code") String code, @RequestParam("newPassword") String newPassword, @RequestParam("mobile") String mobile, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String storedMobile = (String) session.getAttribute("mobile");
        String storedCode = (String) session.getAttribute("authCode");
        Map<String, String> response = new HashMap<>();
        Map<String, String> userPasswords = new HashMap<>();

        if (storedMobile.equals(mobile) && storedCode.equals(code)) {
            userPasswords.put(storedMobile, newPassword);
            response.put("message", "密码重置成功！");
            response.put("flag", "528277b84feab3feb7a2733f09198842");
            response.put("success", "true");
        } else {
            response.put("message", "验证码错误！");
            response.put("success", "false");
        }
        return response;
    }

    private String generateRandomCode() {
        return String.format("%06d", new SecureRandom().nextInt(10000));
    }

    private String generateRandomCode2() {
        return String.format("%04d", new SecureRandom().nextInt(10000));
    }
}
