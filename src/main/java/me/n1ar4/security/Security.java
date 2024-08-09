package me.n1ar4.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Security {
    @SuppressWarnings("all")
    public static void security() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.8")) {
            System.setSecurityManager(new JarAnalyzerSecurityManager());
            System.out.println("[*] LOAD SECURITY MANAGER SUCCESS");
            try {
                Class<?> oifClass = Class.forName("sun.misc.ObjectInputFilter");
                Class<?> oifConfigClass = Class.forName("sun.misc.ObjectInputFilter$Config");
                Class<?> jarOifClass = Class.forName("me.n1ar4.security.JarAnalyzerInputFilter");
                Constructor<?> constructor = jarOifClass.getConstructor(int.class, int.class, int.class, int.class);
                Object jarOif = constructor.newInstance(
                        // MAX ARRAY LENGTH
                        100000,
                        // MAX DEPTH
                        20,
                        // MAX REFS
                        100000,
                        // MAX BYTES
                        500000000);
                Method method = oifConfigClass.getMethod("setSerialFilter", oifClass);
                method.invoke(null, jarOif);
                System.out.println("[*] LOAD OBJECT INPUT FILTER SUCCESS");
            } catch (Exception ignored) {
                System.out.println("[-] LOAD OBJECT INPUT FILTER FAIL");
            }
        } else {
            System.out.println("[*] ONLY JAVA 8 LOAD OBJECT INPUT FILTER");
        }
    }
}
