package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.core.Env;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@SuppressWarnings("all")
public class JarUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final Set<ClassFileEntity> classFileSet = new HashSet<>();

    public static List<ClassFileEntity> resolveNormalJarFile(String jarPath) {
        try {
            Path tmpDir = Paths.get("jar-analyzer-temp/");
            try {
                Files.createDirectory(tmpDir);
            } catch (Exception ignored) {
            }
            resolve(jarPath, tmpDir);
            return new ArrayList<>(classFileSet);
        } catch (Exception e) {
            logger.error("error: {}", e.toString());
        }
        return new ArrayList<>();
    }

    private static void resolve(String jarPathStr, Path tmpDir) {
        Path jarPath = Paths.get(jarPathStr);
        if (!Files.exists(jarPath)) {
            logger.error("jar not exist");
            return;
        }
        try {
            if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".class")) {
                ClassFileEntity classFile = new ClassFileEntity(jarPathStr, jarPath);
                classFile.setJarName("class");
                classFileSet.add(classFile);
            }
            if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".jar") ||
                    jarPathStr.toLowerCase(Locale.ROOT).endsWith(".war")) {
                InputStream is = Files.newInputStream(jarPath);
                JarInputStream jarInputStream = new JarInputStream(is);
                JarEntry jarEntry;
                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    Path fullPath = tmpDir.resolve(jarEntry.getName());
                    if (!jarEntry.isDirectory()) {
                        if (!jarEntry.getName().endsWith(".class")) {
                            if (Env.jarsInJar && jarEntry.getName().endsWith(".jar")) {
                                LogUtil.log("analyze jars in jar");
                                Path dirName = fullPath.getParent();
                                if (!Files.exists(dirName)) {
                                    Files.createDirectories(dirName);
                                }
                                try {
                                    Files.createFile(fullPath);
                                } catch (Exception ignored) {
                                }
                                OutputStream outputStream = Files.newOutputStream(fullPath);
                                IOUtil.copy(jarInputStream, outputStream);
                                doInternal(fullPath, tmpDir);
                                outputStream.close();
                            }
                            continue;
                        }
                        Path dirName = fullPath.getParent();
                        if (!Files.exists(dirName)) {
                            Files.createDirectories(dirName);
                        }
                        OutputStream outputStream = Files.newOutputStream(fullPath);
                        IOUtil.copy(jarInputStream, outputStream);
                        outputStream.close();
                        ClassFileEntity classFile = new ClassFileEntity(jarEntry.getName(), fullPath);
                        String splitStr;
                        if (OSUtil.isWindows()) {
                            splitStr = "\\\\";
                        } else {
                            splitStr = "/";
                        }
                        String[] splits = jarPathStr.split(splitStr);
                        classFile.setJarName(splits[splits.length - 1]);

                        classFileSet.add(classFile);
                    }
                }
                is.close();
                jarInputStream.close();
            }
        } catch (Exception e) {
            logger.error("error: {}", e.toString());
        }
    }

    private static void doInternal(Path jarPath, Path tmpDir) {
        try {
            InputStream is = Files.newInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());
                if (!jarEntry.isDirectory()) {
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }
                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    IOUtil.copy(jarInputStream, outputStream);
                    outputStream.close();
                    ClassFileEntity classFile = new ClassFileEntity(jarEntry.getName(), fullPath);
                    String splitStr;
                    if (OSUtil.isWindows()) {
                        splitStr = "\\\\";
                    } else {
                        splitStr = "/";
                    }
                    String[] splits = jarPath.toString().split(splitStr);
                    classFile.setJarName(splits[splits.length - 1]);

                    classFileSet.add(classFile);
                }
            }
            is.close();
            jarInputStream.close();
        } catch (Exception e) {
            logger.error("error: {}", e.toString());
        }
    }
}