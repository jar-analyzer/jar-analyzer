package me.n1ar4.security;

public class JarAnalyzerClassLoader extends ClassLoader {
    public Class<?> defineClassFromBytes(String className, byte[] classData) {
        return defineClass(className, classData, 0, classData.length);
    }
}
