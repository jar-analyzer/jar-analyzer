package com.best.hello.controller.RCE;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Map;

@RestController
@RequestMapping("/vulnapi/RCE/ProcessImpl")
public class ProcessImplVul {

    @ApiOperation(value = "vul：命令执行 - ProcessImpl", notes = "ProcessImpl是更为底层的实现，Runtime和ProcessBuilder执行命令实际上也是调用了ProcessImpl这个类")
    @RequestMapping("/vul")
    public static String vul(String cmd) throws Exception {

        // ProcessImpl 类是一个抽象类不能直接调用，但可以通过反射来间接调用ProcessImpl来达到执行命令的目的
        // 首先，使用 Class.forName 方法来获取 ProcessImpl 类的类对象
        Class clazz = Class.forName("java.lang.ProcessImpl");

        // 然后，使用 clazz.getDeclaredMethod 方法来获取 ProcessImpl 类的 start 方法
        Method method = clazz.getDeclaredMethod("start", String[].class, Map.class, String.class, ProcessBuilder.Redirect[].class, boolean.class);

        // 使用 method.setAccessible 方法将 start 方法设为可访问
        method.setAccessible(true);

        // 最后，使用 method.invoke 方法来调用 start 方法，并传入参数 cmd，执行命令
        Process process = (Process) method.invoke(null, new String[]{cmd}, null, null, null, false);

        // 获取命令输出
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        return output.toString();
    }

}


