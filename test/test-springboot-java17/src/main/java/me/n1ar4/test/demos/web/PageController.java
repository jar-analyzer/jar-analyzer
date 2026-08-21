package me.n1ar4.test.demos.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pages")
class PageController {

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("title", "首页");
        model.addAttribute("message", "欢迎访问首页");
        return "home"; // 返回视图名称
    }

    @PostMapping("/login")
    public String loginPage(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("loginTime", System.currentTimeMillis());
        return "login-success"; // 返回视图名称
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String aboutPage(Model model) {
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description", "关于我们页面");
        return "about"; // 返回视图名称
    }
}