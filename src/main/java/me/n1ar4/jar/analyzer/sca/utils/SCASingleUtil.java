package me.n1ar4.jar.analyzer.sca.utils;

import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SCASingleUtil {
    public static List<File> nestedJars = new ArrayList<>();

    public static byte[] exploreJar(File file, String keyClassName) {
        byte[] data = null;
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().contains(keyClassName) && !entry.getName().contains("$")) {
                        data = getClassBytes(jarFile, entry);
                        break;
                    }
                } else if (entry.getName().endsWith(".jar")) {
                    File nestedJarFile = SCAExtractor.extractNestedJar(jarFile, entry);
                    nestedJars.add(nestedJarFile);
                }
            }
        } catch (IOException ignored) {
        }
        if (data != null) {
            nestedJars.clear();
            return data;
        }
        if (nestedJars.isEmpty()) {
            return null;
        }
        // 处理内嵌 CLASS
        for (File nest : nestedJars) {
            try (JarFile jarFile = new JarFile(nest)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        if (entry.getName().contains(keyClassName) && !entry.getName().contains("$")) {
                            data = SCASingleUtil.getClassBytes(jarFile, entry);
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
        return data;
    }

    static byte[] getClassBytes(JarFile jarFile, JarEntry entry) throws IOException {
        try (InputStream is = jarFile.getInputStream(entry);
             ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bao.write(buffer, 0, bytesRead);
            }
            return bao.toByteArray();
        }
    }
}
