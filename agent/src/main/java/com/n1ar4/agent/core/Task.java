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

package com.n1ar4.agent.core;

import com.n1ar4.agent.Agent;
import com.n1ar4.agent.transform.CoreTransformer;
import com.n1ar4.agent.util.FilterObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Task implements Runnable {
    private final Socket socket;

    public Task(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            handleSocket();
        } catch (Exception ignored) {
        }
    }

    private void handleSocket() throws Exception {
        ObjectInputStream ois;
        try {
            ois = new FilterObjectInputStream(socket.getInputStream());
        } catch (Exception ex) {
            return;
        }
        String targetClass = (String) ois.readObject();

        if (targetClass.startsWith("<ALL>")) {
            String PASS = targetClass.split("<ALL>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("[-] ERROR PASSWORD");
                return;
            }
            List<String> classNameList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                if (c.getName().startsWith("[")) {
                    continue;
                }
                if (c.getName().contains("$$Lambda")) {
                    continue;
                }
                classNameList.add(c.getName());
            }
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classNameList);
            System.out.printf("[*] write length %d to socket\n", classNameList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<FILTERS>")) {
            String PASS = targetClass.split("<FILTERS>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("[-] ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("javax.servlet.Filter");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.printf("[*] write length %d to socket\n", classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<VALVES>")) {
            String PASS = targetClass.split("<VALVES>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("[-] ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("org.apache.catalina.Valve");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.printf("[*] write length %d to socket\n", classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<SERVLETS>")) {
            String PASS = targetClass.split("<SERVLETS>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("[-] ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("javax.servlet.Servlet");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.printf("[*] write length %d to socket\n", classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<LISTENERS>")) {
            String PASS = targetClass.split("<LISTENERS>")[1];
            if (!PASS.equals(Agent.PASSWORD)) {
                System.out.println("[-] ERROR PASSWORD");
                return;
            }
            List<String> classList = new ArrayList<>();
            for (Class<?> c : Agent.staticClasses) {
                try {
                    ClassLoader classLoader;
                    if (c.getClassLoader() != null) {
                        classLoader = c.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }

                    Class<?> clsFilter = null;
                    try {
                        clsFilter = classLoader.loadClass("javax.servlet.ServletRequestListener");
                    } catch (Exception ignored) {
                    }

                    if (clsFilter != null && clsFilter.isAssignableFrom(c)) {
                        classList.add(c.getName());
                    }
                } catch (Exception ignored) {
                }
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(classList);
            System.out.printf("[*] write length %d to socket\n", classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (!targetClass.split("\\|")[0].equals(Agent.PASSWORD)) {
            return;
        }
        targetClass = targetClass.split("\\|")[1];
        for (Class<?> c : Agent.staticClasses) {
            if (c.getName().equals(targetClass)) {
                CoreTransformer coreTransformer = new CoreTransformer(targetClass);
                Agent.staticIns.addTransformer(coreTransformer, true);
                Agent.staticIns.retransformClasses(c);

                if (coreTransformer.data != null && coreTransformer.data.length != 0) {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bao);
                    oos.writeObject(coreTransformer.data);
                    System.out.printf("[*] write length %d to socket\n", coreTransformer.data.length);
                    socket.getOutputStream().write(bao.toByteArray());
                }
                Agent.staticIns.removeTransformer(coreTransformer);
            }
        }
    }
}
