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

    @SuppressWarnings("all")
    public static void agentmain(String agentArgs, Instrumentation ins) {
        if (agentArgs == null || agentArgs.trim().equals("")) {
            agentArgs = "12345678";
            System.out.println("default password : " + agentArgs);
        }
        if (agentArgs.length() != 8) {
            return;
        }
        PASSWORD = agentArgs;
        staticIns = ins;

        new Thread(() -> {
            try {
                int port = 10033;
                ServerSocket s = null;
                try {
                    s = new ServerSocket(port);
                } catch (Exception e) {
                    return;
                } finally {
                    if (s != null) {
                        s.close();
                    }
                }
                ServerSocket ss = new ServerSocket(port);
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
    public static void premain(String agentArgs, final Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

}