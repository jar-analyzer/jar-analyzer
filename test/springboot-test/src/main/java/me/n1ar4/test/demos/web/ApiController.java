package me.n1ar4.test.demos.web;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
class ApiController {

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", "用户列表");
        return result;
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, String> user) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "created");
        result.put("user", user);
        return result;
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String getInfo() {
        return "API信息";
    }
}