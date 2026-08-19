package com.best.hello.controller;

import com.best.hello.util.JwtUtils;
import com.wf.captcha.utils.CaptchaUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class Login {
    @Value("${local.admin.name}")
    private String user;

    @Value("${local.admin.password}")
    private String pass;

    private static final String COOKIE_NAME = "JWT_TOKEN";

    @ApiOperation(value = "登录")
    @RequestMapping("/user/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("captcha") String captcha, Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response) {

        // 处理重定向
        String referer = request.getHeader("referer");

        // 验证码复用
        if (!CaptchaUtil.ver(captcha, request)) {
            CaptchaUtil.clear(request);
            model.addAttribute("msg", "验证码不正确");
            return "login";
        }

        if (user.equals(username) && pass.equals(password)) {
            // 创建JWT Token
            String token = JwtUtils.generateToken(username);
            Cookie cookie = new Cookie(COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(60 * 60 * 24);
            cookie.setPath("/");
            response.addCookie(cookie);
            session.setAttribute("LoginUser", username);

            if (referer == null || referer.isEmpty() || referer.contains("/login") || referer.contains("/user/ldap") || referer.contains("/user/logout")) {
                return "redirect:/index";
            } else {
                return "redirect:" + referer;
            }

        } else {
            model.addAttribute("msg", "用户名或者密码错误");
            return "login";
        }
    }

    @ApiOperation(value = "注销")
    @GetMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @ApiOperation(value = "验证码")
    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CaptchaUtil.out(request, response);
    }
}
