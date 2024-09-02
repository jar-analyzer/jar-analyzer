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

package me.n1ar4.jar.analyzer.sca.utils;

import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SCAMultiUtil {
    public static List<File> nestedJars = new ArrayList<>();

    public static Map<String, byte[]> exploreJarEx(File file, Map<String, String> hashMap) {
        Map<String, byte[]> resultMap = new HashMap<>();
        for (Map.Entry<String, String> mapEntry : hashMap.entrySet()) {
            String keyClass = mapEntry.getKey();
            // 处理直接 CLASS
            try (JarFile jarFile = new JarFile(file)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        if (entry.getName().contains(keyClass) && !entry.getName().contains("$")) {
                            byte[] data = SCASingleUtil.getClassBytes(jarFile, entry);
                            resultMap.put(keyClass, data);
                            break;
                        }
                    } else if (entry.getName().endsWith(".jar")) {
                        // 这里别递归了 要不然变成一坨
                        File nestedJarFile = SCAExtractor.extractNestedJar(jarFile, entry);
                        nestedJars.add(nestedJarFile);
                    }
                }
            } catch (IOException ignored) {
            }
            if (nestedJars.isEmpty()) {
                continue;
            }
            // 处理内嵌 CLASS
            for (File nest : nestedJars) {
                try (JarFile jarFile = new JarFile(nest)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            if (entry.getName().contains(keyClass) && !entry.getName().contains("$")) {
                                byte[] data = SCASingleUtil.getClassBytes(jarFile, entry);
                                resultMap.put(keyClass, data);
                                break;
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
            for (File nestedJar : nestedJars) {
                boolean success = nestedJar.delete();
                if (!success) {
                    LogUtil.warn("delete temp jar fail");
                }
            }
            nestedJars.clear();
        }
        return resultMap;
    }
}
