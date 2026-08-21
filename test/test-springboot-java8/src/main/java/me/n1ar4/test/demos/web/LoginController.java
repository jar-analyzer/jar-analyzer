package me.n1ar4.test.demos.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @ResponseBody
    public String login(HttpServletRequest request, Model model) {
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"系统异常\"}";
        }
        return null;
    }
}