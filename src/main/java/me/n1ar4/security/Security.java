package me.n1ar4.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Security {
    private static final int maxArrayLength = 100000;
    private static final int maxDepth = 20;
    private static final int maxRefs = 100000;
    private static final int maxBytes = 500000000;

    @SuppressWarnings("all")
    public static void setObjectInputFilter() {
        Class<?> jarOifClass;
        Class<?> oifClass;
        Class<?> oifConfigClass;

        // JAVA VERSION
        String version = System.getProperty("java.version");
        if (!version.startsWith("1.8")) {
            try {
                byte[] data = JarAnalyzerInputFilter9Dump.makeJava9();
                JarAnalyzerClassLoader loader = new JarAnalyzerClassLoader();
                jarOifClass = loader.defineClassFromBytes(JarAnalyzerInputFilter9Dump.getClassName(), data);
                oifClass = Class.forName("java.io.ObjectInputFilter");
                oifConfigClass = Class.forName("java.io.ObjectInputFilter$Config");
            } catch (Exception ignored) {
                System.out.println("[-] JAVA 9+ LOAD OBJECT INPUT FILTER FAIL");
                return;
            }
        } else {
            try {
                byte[] data = JarAnalyzerInputFilter8Dump.makeJava8();
                JarAnalyzerClassLoader loader = new JarAnalyzerClassLoader();
                jarOifClass = loader.defineClassFromBytes(JarAnalyzerInputFilter8Dump.getClassName(), data);
                oifClass = Class.forName("sun.misc.ObjectInputFilter");
                oifConfigClass = Class.forName("sun.misc.ObjectInputFilter$Config");
            } catch (Exception ignored) {
                System.out.println("[-] JAVA 8 LOAD OBJECT INPUT FILTER FAIL");
                return;
            }
        }

        try {
            Constructor<?> constructor = jarOifClass.getConstructor(int.class, int.class, int.class, int.class);
            Object jarOif = constructor.newInstance(maxArrayLength, maxDepth, maxRefs, maxBytes);
            Method method = oifConfigClass.getMethod("setSerialFilter", oifClass);
            System.out.println("[*] LOAD OBJECT INPUT FILTER SUCCESS");
        } catch (Exception ignored) {
            System.out.println("[-] LOAD OBJECT INPUT FILTER FAIL");
        }
    }

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
