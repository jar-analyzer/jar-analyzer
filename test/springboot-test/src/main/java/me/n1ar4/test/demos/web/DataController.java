package me.n1ar4.test.demos.web;

import me.n1ar4.test.service.RCEService;
import me.n1ar4.test.util.Func;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/data")
class DataController {

    @Autowired
    RCEService rceService;

    @GetMapping("/status")
    @ResponseBody
    public Map<String, String> getStatus(String cmd) {
        Map<String, String> status = new HashMap<>();
        status.put("server", "running");
        status.put("time", String.valueOf(System.currentTimeMillis()));
        String newCmd = new Func().getCmd(cmd);
        rceService.test(newCmd);
        return status;
    }

    @PostMapping("/submit")
    @ResponseBody
    public String submitData(@RequestParam String data) {
        return "数据已提交: " + data;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    @ResponseBody
    public String healthCheck() {
        return "健康检查通过";
    }
}