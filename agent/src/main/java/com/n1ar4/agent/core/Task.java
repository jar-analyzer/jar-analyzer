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

import arthas.VmTool;
import com.n1ar4.agent.Agent;
import com.n1ar4.agent.dto.ResultReturn;
import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.UrlInfo;
import com.n1ar4.agent.service.ServerDiscovery;
import com.n1ar4.agent.service.ServerDiscoveryType;
import com.n1ar4.agent.transform.CoreTransformer;
import com.n1ar4.agent.util.Base64Util;
import com.n1ar4.agent.util.CustomOutputStream;
import com.n1ar4.agent.util.FilterObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.net.Socket;
import java.util.*;

public class Task implements Runnable {
    private final Socket socket;
    private final VmTool vmTool;
    private final Instrumentation instLocal;

    private static final HashSet<ClassLoader> loaders = new HashSet<>();

    public Task(Socket socket, VmTool vmTool, Instrumentation instLocal) {
        this.socket = socket;
        this.vmTool = vmTool;
        this.instLocal = instLocal;
        Set<ClassLoader> classLoaders = new HashSet<>();
        Class<?>[] allLoadedClasses = Agent.staticIns.getAllLoadedClasses();
        for (Class<?> loadedClass : allLoadedClasses) {
            ClassLoader classLoader = loadedClass.getClassLoader();
            if (classLoader != null) {
                classLoaders.add(classLoader);
            }
        }
        loaders.addAll(classLoaders);
    }

    @Override
    public void run() {
        try {
            handleSocket();
        } catch (Exception ignored) {
        }
    }

    public static ArrayList<SourceResult> mergeSourceResults(HashSet<SourceResult> results) {
        ArrayList<SourceResult> newResult = new ArrayList<>();
        HashMap<String, ArrayList<SourceResult>> SourceCollectList = new HashMap<>();
        for (SourceResult resultItem : results) {
            String index = String.format("%s|%s+%s", resultItem.getType().toString(),
                    resultItem.getSourceClass(), resultItem.getMethodInfo());
            if (!SourceCollectList.containsKey(index))
                SourceCollectList.put(index, new ArrayList<>());
            SourceCollectList.get(index).add(resultItem);
        }
        for (ArrayList<SourceResult> sourceResults : SourceCollectList.values()) {
            SourceResult originalSourceResult = sourceResults.get(0);
            ArrayList<UrlInfo> urlInfos = new ArrayList<>();
            ArrayList<String> descriptions = new ArrayList<>();
            for (SourceResult sourceResult : sourceResults) {
                ArrayList<UrlInfo> nowUrlInfos = sourceResult.getUrlInfos();
                if (nowUrlInfos != null) {
                    for (UrlInfo s : nowUrlInfos) {
                        s.appendDescription(SourceResult.SourceResultTag + sourceResult.hashCode());
                        urlInfos.add(s);
                    }
                }
                ArrayList<String> value;
                descriptions.add(SourceResult.SourceResultTag + sourceResult.hashCode());
                ArrayList<String> description = sourceResult.getDescription();
                if ((value = description) != null) {
                    for (String s : value) {
                        descriptions.add("\t" + s);
                    }
                }
            }
            newResult.add(new SourceResult(
                    originalSourceResult.getType(),
                    originalSourceResult.getName(),
                    originalSourceResult.getSourceClass(),
                    originalSourceResult.getMethodInfo(),
                    urlInfos,
                    descriptions
            ));
        }
        return newResult;
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
            Agent.refreshClass();
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
            System.out.printf("[*] WRITE TOTAL CLASS %d\n", classNameList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<GET-ALL>")) {
            Agent.refreshClass();
            ResultReturn resultReturn = new ResultReturn("", "");
            try {
                String PASS = targetClass.split("<GET-ALL>")[1];
                if (!PASS.equals(Agent.PASSWORD)) {
                    System.out.println("[-] ERROR PASSWORD");
                    return;
                }
                ArrayList<SourceResult> sourceResults = new ArrayList<>();
                for (ServerDiscoveryType serverDiscoveryType : ServerDiscoveryType.values()) {
                    ServerDiscovery serverDiscovery = serverDiscoveryType.getServerDiscovery();
                    if (!serverDiscovery.CanLoad(vmTool, instLocal)) {
                        continue;
                    }
                    sourceResults.addAll(serverDiscovery.getServerSources(vmTool, instLocal));
                }
                ArrayList<SourceResult> sourceResultsFinal = mergeSourceResults(new HashSet<>(sourceResults));
                Collections.sort(sourceResultsFinal);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bao);
                oos.writeObject(sourceResultsFinal);
                oos.close();
                resultReturn.setObjectString(Base64Util.encode(bao.toByteArray()));
            } catch (Exception e) {
                CustomOutputStream customOutputStream = new CustomOutputStream();
                PrintStream printStream = new PrintStream(customOutputStream);
                e.printStackTrace(printStream);
                resultReturn.setConsoleOutput(customOutputStream.getResult());
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(resultReturn);
            oos.close();
            System.out.printf("[*] WRITE OBJECT LENGTH %d\n", resultReturn.getObjectString().length());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (targetClass.startsWith("<VALVES>")) {
            Agent.refreshClass();
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
            System.out.printf("[*] WRITE TOTAL CLASS %d\n", classList.size());
            socket.getOutputStream().write(bao.toByteArray());
            return;
        }

        if (!targetClass.split("\\|")[0].equals(Agent.PASSWORD)) {
            return;
        }
        targetClass = targetClass.split("\\|")[1];
        Agent.refreshClass();

        try {
            Class<?> c = loadClassUsingAllClassLoaders(targetClass);
            if (c == null) {
                throw new RuntimeException("CLASS NOT FOUND");
            }
            CoreTransformer coreTransformer = new CoreTransformer(targetClass);
            Agent.staticIns.addTransformer(coreTransformer, true);
            Agent.staticIns.retransformClasses(c);
            if (coreTransformer.data != null && coreTransformer.data.length != 0) {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bao);
                oos.writeObject(coreTransformer.data);
                System.out.printf("[*] WRITE BYTECODE LENGTH %d\n", coreTransformer.data.length);
                socket.getOutputStream().write(bao.toByteArray());
            }
            Agent.staticIns.removeTransformer(coreTransformer);
        } catch (Exception ignored) {
            System.out.println("[-] CLASS NOT FOUND: " + targetClass);
        }
    }

    public static Class<?> loadClassUsingAllClassLoaders(String className) {
        // 使用缓存 LOADERS 先找一遍
        for (ClassLoader classLoader : loaders) {
            try {
                return Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        // 如果所有 classLoader 都找不到 刷新一次
        Agent.refreshClass();
        Set<ClassLoader> classLoaders = new HashSet<>();
        Class<?>[] allLoadedClasses = Agent.staticIns.getAllLoadedClasses();
        for (Class<?> loadedClass : allLoadedClasses) {
            ClassLoader classLoader = loadedClass.getClassLoader();
            if (classLoader != null) {
                classLoaders.add(classLoader);
            }
        }
        // 刷新全局 LOADERS
        loaders.clear();
        loaders.addAll(classLoaders);
        // 再重新找一次
        for (ClassLoader classLoader : classLoaders) {
            try {
                return Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}
