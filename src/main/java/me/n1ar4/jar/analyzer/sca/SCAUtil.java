package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class SCAUtil {
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("all")
    public static byte[] exploreJar(File file, String keyClassName) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().contains(keyClassName)) {
                        return getClassBytes(jarFile, entry);
                    }
                } else if (entry.getName().endsWith(".jar")) {
                    File nestedJarFile = extractNestedJar(jarFile, entry);
                    exploreJar(nestedJarFile, keyClassName);
                    nestedJarFile.delete();
                }
            }
        } catch (IOException e) {
            logger.error(e.toString());
            SCALogger.logger.error(e.toString()); // 如果需要，也可以保留这行
        }
        return null;
    }

    @SuppressWarnings("all")
    private static File extractNestedJar(JarFile jarFile, JarEntry entry) throws IOException {
        File tempFile = File.createTempFile("nested-", ".jar");
        try (JarInputStream jarInputStream = new JarInputStream(jarFile.getInputStream(entry));
             FileInputStream fis = new FileInputStream(tempFile)) {
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                fis.read(buffer, 0, bytesRead);
            }
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
