package com.best.hello.controller.RCE;

import com.best.hello.util.Security;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/vulnapi/RCE/ProcessBuilder")
public class ProcessBuilderVul {

    /**
     * @poc http://127.0.0.1:8888/RCE/ProcessBuilder/vul?filepath=/tmp;whoami
     */
    @ApiOperation(value = "vul：命令执行 - ProcessBuilder", notes = "调用ProcessBuilder执行ls命令，接收参数filepath，拼接命令语句")
    @RequestMapping("/vul")
    public static String processbuilderVul(String filepath) throws IOException {

        String[] cmdList = {"sh", "-c", "ls -l " + filepath};

        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        return output.toString();
    }


    /**
     * @poc http://127.0.0.1:8888/RCE/ProcessBuilder/safe?filepath=/tmp;whoami
     */
    @ApiOperation(value = "safe: 过滤特殊字符")
    @RequestMapping("/safe")
    public static String processbuilderSafe(String filepath) throws IOException {

        if (!Security.checkOs(filepath)) {

            String[] cmdList = {"sh", "-c", "ls -l " + filepath};

            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            return output.toString();
        } else {
            return "检测到非法命令注入";
        }
    }

}
