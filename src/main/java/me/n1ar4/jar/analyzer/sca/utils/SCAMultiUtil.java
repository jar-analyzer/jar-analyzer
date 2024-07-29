package me.n1ar4.jar.analyzer.sca.utils;

import cn.hutool.core.lang.UUID;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SCAMultiUtil {
    public static List<File> nestedJars = new ArrayList<>();

    public static boolean exploreJarEx(File file, Map<String, String> hashMap) {
        Map<String, Boolean> resultMap = new HashMap<>();
        for (Map.Entry<String, String> mapEntry : hashMap.entrySet()) {
            String keyClass = mapEntry.getKey();
            String hash = mapEntry.getValue();
            // 处理直接 CLASS
            try (JarFile jarFile = new JarFile(file)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        if (entry.getName().contains(keyClass) && !entry.getName().contains("$")) {
                            byte[] data = SCASingleUtil.getClassBytes(jarFile, entry);
                            String result = SCAHashUtil.sha256(data);
                            if (hash.equals(result)) {
                                resultMap.put(keyClass, true);
                                break;
                            }
                        }
                    } else if (entry.getName().endsWith(".jar")) {
                        // 这里别递归了 要不然变成一坨
                        File nestedJarFile = extractNestedJar(jarFile, entry);
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
                                String result = SCAHashUtil.sha256(data);
                                if (hash.equals(result)) {
                                    resultMap.put(keyClass, true);
                                    break;
                                }
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
        return resultMap.size() == hashMap.size();
    }

    private static File extractNestedJar(JarFile jarFile, JarEntry entry) throws IOException {
        if (jarFile == null) {
            throw new IllegalArgumentException("JarFile cannot be null");
        }
        if (entry == null) {
            throw new IllegalArgumentException("JarEntry cannot be null");
        }
        if (jarFile.getJarEntry(entry.getName()) == null) {
            throw new IOException("JarEntry does not exist in the JarFile");
        }
        Path finalDir = Paths.get(Const.tempDir).resolve("SCA");
        try {
            Files.createDirectories(finalDir);
        } catch (Exception ignored) {
        }
        File tempFile = Files.createFile(finalDir.resolve(
                String.format("%s.jar", UUID.randomUUID()))).toFile();
        try (InputStream jarInputStream = jarFile.getInputStream(entry);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            if (jarInputStream == null) {
                throw new IOException("Could not get InputStream for JarEntry");
            }
            IOUtil.copy(jarInputStream, fos);
        }
        return tempFile;
    }
}
