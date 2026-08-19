package com.best.hello.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api("跨站请求伪造")
@RestController
@RequestMapping("/vulnapi/CSRF")
public class CSRF {
    @ApiOperation(value = "vul: 危险的转账")
    @GetMapping("/transfer/vul")
    public Map<String, Object> transferMoney(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        // 从请求中获取转账金额和接收者
        String from = (String) session.getAttribute("LoginUser");
        String amount = request.getParameter("amount");
        String receiver = request.getParameter("receiver");

        Map<String, Object> result = new HashMap<>();
        result.put("from", from);
        result.put("receiver", receiver);
        result.put("amount", amount);
        result.put("success", true);
        return result;
    }

    @ApiOperation(value = "vul: referer绕过", notes = "通过referer限制，只允许本站发起的请求，但是referer可以伪造")
    @GetMapping("/transfer/referer")
    public Map<String, Object> transferMoneySafe(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String from = (String) session.getAttribute("LoginUser");
        String amount = request.getParameter("amount");
        String receiver = request.getParameter("receiver");
        Map<String, Object> result = new HashMap<>();
        // 校验Referer 判断请求是否来自本站
        String referer = request.getHeader("referer");
        if (referer == null || !referer.startsWith("http://baidu.com")) {
            result.put("success", false);
            result.put("message", "referer is not valid");
            return result;
        }
        result.put("from", from);
        result.put("receiver", receiver);
        result.put("amount", amount);
        result.put("success", true);
        return result;
    }

    @GetMapping("/transfer/genCSRFToken")
    public Map<String, Object> genCSRFToken(HttpSession session, Model model) {
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        Map<String, Object> result = new HashMap<>();
        result.put("csrfToken", token);
        return result;
    }

    @PostMapping("/transfer/doTransferToken")
    public Map<String, Object> doTransferToken(HttpServletRequest request, HttpSession session) {
        String token = request.getParameter("csrfToken");
        String sessionToken = (String) session.getAttribute("csrfToken");
        String from = (String) session.getAttribute("LoginUser");
        String amount = request.getParameter("amount");
        String receiver = request.getParameter("receiver");
        Map<String, Object> result = new HashMap<>();

        // 校验CSRF Token
        if (!token.equals(sessionToken)) {
            result.put("success", false);
            result.put("message", "token is not valid");
            return result;
        }

        result.put("from", from);
        result.put("receiver", receiver);
        result.put("amount", amount);
        result.put("csrfToken", token);
        result.put("success", true);
        return result;
    }

}
