/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.n1ar4.agent;

import com.n1ar4.agent.core.Task;

import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

public class Agent {
    public static String PASSWORD;
    public static Instrumentation staticIns;
    public static Class<?>[] staticClasses;
    private static final String DEFAULT_PASSWD = "12345678";
    private static final int DEFAULT_PORT = 10033;

    /**
     * 动态的 Agent 方式
     * 通过 tools 的 attach
     *
     * @param agentArgs
     * @param ins
     */
    @SuppressWarnings("all")
    public static void agentmain(String agentArgs, Instrumentation ins) {
        String passwd = null;
        int port = -1;
        if (agentArgs == null || agentArgs.trim().equals("")) {
            passwd = DEFAULT_PASSWD;
            port = DEFAULT_PORT;
        } else {
            // usage: java -javaagent:agent.jar=port=10033;password=P4sSW0rD
            agentArgs = agentArgs.trim();
            String[] parts = agentArgs.split(";");
            for (String part : parts) {
                if (part.contains("=")) {
                    String key = part.split("=")[0];
                    String val = part.split("=")[1];
                    if (key.equals("password")) {
                        passwd = val;
                    }
                    if (key.equals("port")) {
                        port = Integer.parseInt(val);
                    }
                }
            }
        }
        if (passwd == null || port == -1) {
            return;
        }
        PASSWORD = passwd;
        staticIns = ins;

        System.out.println("###################### JAR ANALYZER ######################");
        System.out.println("[*] agent password : " + passwd);
        System.out.println("[*] agent port : " + port);
        System.out.println("##########################################################");

        int finalPort = port;
        new Thread(() -> {
            try {
                ServerSocket s = null;
                try {
                    s = new ServerSocket(finalPort);
                } catch (Exception e) {
                    return;
                } finally {
                    if (s != null) {
                        s.close();
                    }
                }
                ServerSocket ss = new ServerSocket(finalPort);
                while (true) {
                    Socket socket = ss.accept();
                    staticClasses = (Class<?>[]) ins.getAllLoadedClasses();
                    new Thread(new Task(socket)).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * 启动前的 Agent
     * 通过 -javaagent 参数启动
     * -javaagent:agent.jar
     *
     * @param agentArgs 输入参数
     * @param inst      inst
     */
    public static void premain(String agentArgs, final Instrumentation inst) {
        agentmain(agentArgs, inst);
    }
}