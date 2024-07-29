package me.n1ar4.jar.analyzer.sca.utils;

import me.n1ar4.jar.analyzer.utils.IOUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SCAUtil {
    public static List<String> visitedJar = new ArrayList<>();
    public static boolean found = false;
    public static byte[] data = null;

    public static void refresh() {
        visitedJar.clear();
        found = false;
        data = null;
    }

    @SuppressWarnings("all")
    public static byte[] exploreJar(File file, String keyClassName) {
        if (found) {
            return data;
        }
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().contains(keyClassName) && !entry.getName().contains("$")) {
                        found = true;
                        data = getClassBytes(jarFile, entry);
                        return data;
                    }
                } else if (entry.getName().endsWith(".jar")) {
                    if (visitedJar.contains(entry.getName())) {
                        continue;
                    }
                    File nestedJarFile = extractNestedJar(jarFile, entry);
                    exploreJar(nestedJarFile, keyClassName);
                    visitedJar.add(entry.getName());
                    nestedJarFile.delete();
                }
            }
        } catch (IOException ignored) {
        }
        return data;
    }

    @SuppressWarnings("all")
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
        File tempFile = File.createTempFile("nested-", ".jar");
        try (InputStream jarInputStream = jarFile.getInputStream(entry);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            if (jarInputStream == null) {
                throw new IOException("Could not get InputStream for JarEntry");
            }
            IOUtil.copy(jarInputStream, fos);
        }
        return tempFile;
    }

    private static byte[] getClassBytes(JarFile jarFile, JarEntry entry) throws IOException {
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
