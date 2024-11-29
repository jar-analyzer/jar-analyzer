/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.security;

public class JarAnalyzerClassLoader extends ClassLoader {
    public Class<?> defineClassFromBytes(String className, byte[] classData) {
        return defineClass(className, classData, 0, classData.length);
    }
}
