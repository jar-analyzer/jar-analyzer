/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.security;

import java.lang.reflect.Method;

public class Security {
    @SuppressWarnings("all")
    public static void setSecurityManager() {
        // JAVA VERSION
        String version = System.getProperty("java.version");
        if (!version.startsWith("1.8")) {
            System.out.println("[*] ONLY JAVA 8 LOAD SECURITY MANAGER");
            return;
        }
        // LOAD SECURITY MANAGER
        try {
            Class<?> systemClz = Class.forName("java.lang.System");
            Class<?> smClz = Class.forName("java.lang.SecurityManager");
            Class<?> jarClz = Class.forName("me.n1ar4.security.JarAnalyzerSecurityManager");
            Method setSec = systemClz.getMethod("setSecurityManager", smClz);
            Object jarSm = jarClz.newInstance();
            setSec.invoke(null, jarSm);
            System.out.println("[*] JAVA 8 LOAD SECURITY MANAGER SUCCESS");
        } catch (Exception ignored) {
            System.out.println("[-] JAVA 8 LOAD OBJECT INPUT FILTER FAIL");
        }
        System.setSecurityManager(new JarAnalyzerSecurityManager());
    }
}
