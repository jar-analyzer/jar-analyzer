package me.n1ar4.jar.analyzer.sca.utils;

import cn.hutool.core.lang.UUID;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SCAExtractor {
    public static File extractNestedJar(JarFile jarFile, JarEntry entry) throws IOException {
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
