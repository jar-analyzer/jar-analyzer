package com.best.hello.controller.RCE;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/vulnapi/RCE/Runtime")
public class RuntimeVul {

    /**
     * @poc http://127.0.0.1:8888/RCE/Runtime/vul?cmd=id
     */
    @ApiOperation(value = "vul：命令执行 - Runtime.getRuntime().exec()")
    @RequestMapping("/vul")
    public static String vul(String cmd) {
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            Process proc = Runtime.getRuntime().exec(cmd);

            // 读取命令的输出
            InputStream inputStream = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @ApiOperation(value = "safe: 白名单方式")
    @RequestMapping("/safe")
    public static String safe(String cmd) {
        StringBuilder sb = new StringBuilder();
        String line;

        // 定义命令白名单
        Set<String> commands = new HashSet<>();
        commands.add("ls");
        commands.add("pwd");

        // 检查用户提供的命令是否在白名单中
        String command = cmd.split("\\s+")[0];
        if (!commands.contains(command)) {
            return "不在白名单中";
        }

        try {
            Process proc = Runtime.getRuntime().exec(cmd);

            InputStream inputStream = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    @ApiOperation(value = "vul：Runtime.getRuntime().load()")
    @GetMapping("/vul2")
    public static void vul2(String file) {
        try {
            Runtime rt = Runtime.getRuntime();
            // 加载动态链接库
            rt.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
