/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent;

import com.agent.vmTool.VmTool;
import com.n1ar4.agent.core.Task;

import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

import static com.n1ar4.agent.vmtools.VmToolUtils.getVmToolInstances;

public class Agent {
    public static String PASSWORD;
    public static Instrumentation staticIns;
    public static Class<?>[] staticClasses;
    private static final String DEFAULT_PASSWD = "12345678";
    private static final int DEFAULT_PORT = 10033;

    public static void refreshClass() {
        staticClasses = (Class<?>[]) staticIns.getAllLoadedClasses();
    }

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
        VmTool vmToolInstances = getVmToolInstances();
        if (vmToolInstances == null) {
            System.out.println("[-] LOAD ERROR");
            return;
        }

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
                    new Thread(new Task(socket, vmToolInstances, ins)).start();
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