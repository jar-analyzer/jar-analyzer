/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.ListParser;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("all")
public class JarUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final Set<ClassFileEntity> classFileSet = new HashSet<>();

    private static final String META_INF = "META-INF";
    private static final int MAX_PARENT_SEARCH = 20;

    // 配置文件扩展名列表
    public static final Set<String> CONFIG_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".yml", ".yaml", ".properties", ".xml", ".json", ".conf", ".config", ".ini", ".toml", "web.xml"
    ));

    public static boolean isConfigFile(String fileName) {
        fileName = fileName.toLowerCase();
        for (String ext : CONFIG_EXTENSIONS) {
            if (fileName.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static List<ClassFileEntity> resolveNormalJarFile(String jarPath, Integer jarId) {
        try {
            Path tmpDir = Paths.get(Const.tempDir);
            classFileSet.clear();
            resolve(jarId, jarPath, tmpDir);
            return new ArrayList<>(classFileSet);
        } catch (Exception e) {
            logger.error("error: {}", e.toString());
        }
        return new ArrayList<>();
    }

    private static boolean shouldRun(String whiteText, String blackText, String saveClass) {
        boolean whiteDoIt = false;

        // 处理 BOOT-INF WEB-INF 的问题
        int i = saveClass.indexOf("classes");
        if (i > 0) {
            if (saveClass.contains("BOOT-INF") || saveClass.contains("WEB-INF")) {
                saveClass = saveClass.substring(i + 8, saveClass.length() - 6);
            } else {
                saveClass = saveClass.substring(0, saveClass.length() - 6);
            }
        }

        if (whiteText != null && !StringUtil.isNull(whiteText)) {
            ArrayList<String> data = ListParser.parse(whiteText);
            String className = saveClass;
            if (className.endsWith(".class")) {
                className = className.substring(0, className.length() - 6);
            }
            for (String s : data) {
                // PACAKGE
                if (s.endsWith("/")) {
                    if (className.startsWith(s)) {
                        whiteDoIt = true;
                        break;
                    }
                } else {
                    // CLASSNAME
                    if (className.equals(s)) {
                        whiteDoIt = true;
                        break;
                    }
                }
            }
            if (data == null || data.size() == 0) {
                whiteDoIt = true;
            }
        } else {
            whiteDoIt = true;
        }

        if (!whiteDoIt) {
            return false;
        }

        boolean doIt = true;
        if (blackText != null && !StringUtil.isNull(blackText)) {
            ArrayList<String> data = ListParser.parse(blackText);
            String className = saveClass;
            if (className.endsWith(".class")) {
                className = className.substring(0, className.length() - 6);
            }
            for (String s : data) {
                // com.a.TestClass
                if (className.equals(s)) {
                    doIt = false;
                    break;
                }
                // com.a.
                if (s.endsWith("/")) {
                    if (className.startsWith(s)) {
                        doIt = false;
                        break;
                    }
                }
            }
        }

        if (!doIt) {
            return false;
        }

        return true;
    }

    private static void resolve(Integer jarId, String jarPathStr, Path tmpDir) {
        String text = MainForm.getInstance().getClassBlackArea().getText();
        String whiteText = MainForm.getInstance().getClassWhiteArea().getText();
        Path jarPath = Paths.get(jarPathStr);
        if (!Files.exists(jarPath)) {
            logger.error("jar not exist");
            return;
        }
        try {
            if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".class")) {
                String fileText = MainForm.getInstance().getFileText().getText().trim();
                if (jarPathStr.contains(fileText)) {
                    String backPath = jarPathStr;

                    // #################################################
                    // 2025/06/26 处理重大 BUG
                    // 加载单个 CLASS 时 CLASSNAME 按照 META-INF 决定
                    Path parentPath = jarPath;
                    Path resultPath = null;
                    // 循环找 META-INF 目录
                    int index = 0;
                    while ((parentPath = parentPath.getParent()) != null) {
                        Path metaPath = parentPath.resolve("META-INF");
                        if (Files.exists(metaPath)) {
                            resultPath = metaPath;
                            break;
                        }
                        index++;
                        // 防止一直循环
                        if (index > MAX_PARENT_SEARCH) {
                            break;
                        }
                    }
                    if (resultPath == null) {
                        return;
                    }
                    String finalPath = resultPath.toAbsolutePath().toString();
                    if (!finalPath.contains(fileText)) {
                        // 跨越目录除外
                        return;
                    }
                    // 防止预期外错误
                    if (finalPath.length() < META_INF.length()) {
                        logger.warn("路径长度不足: {}", finalPath);
                        return;
                    }
                    try {
                        jarPathStr = jarPathStr.substring(finalPath.length() - META_INF.length());
                    } catch (StringIndexOutOfBoundsException e) {
                        logger.error("字符串截取错误: jarPathStr={}, finalPath={}", jarPathStr, finalPath, e);
                        return;
                    }
                    String saveClass = jarPathStr.replace("\\", "/");
                    logger.info("加载 CLASS 文件 {}", saveClass);
                    // #################################################

                    if (!shouldRun(whiteText, text, saveClass)) {
                        return;
                    }

                    ClassFileEntity classFile = new ClassFileEntity(saveClass, jarPath, jarId);
                    classFile.setJarName("class");
                    classFileSet.add(classFile);

                    Path fullPath = tmpDir.resolve(jarPathStr);
                    Path parPath = fullPath.getParent();
                    if (!Files.exists(parPath)) {
                        Files.createDirectories(parPath);
                    }
                    try {
                        Files.createFile(fullPath);
                    } catch (Exception ignored) {
                    }
                    InputStream fis = Files.newInputStream(Paths.get(backPath));
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    IOUtil.copy(fis, outputStream);
                    outputStream.close();
                    fis.close();
                } else {
                    return;
                }
            } else if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".jar") ||
                    jarPathStr.toLowerCase(Locale.ROOT).endsWith(".war")) {
                ZipFile jarFile = new ZipFile(jarPath);
                Enumeration<? extends ZipArchiveEntry> entries = jarFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry jarEntry = entries.nextElement();
                    // =============== 2024/04/26 修复 ZIP SLIP 漏洞 ===============
                    String jarEntryName = jarEntry.getName();
                    // 第一次检查是否包含 ../ ..\\ 绕过
                    if (jarEntryName.contains("../") || jarEntryName.contains("..\\")) {
                        logger.warn("detect zip slip vulnearbility");
                        // 不抛出异常只跳过这个文件继续处理其他文件
                        continue;
                    }
                    // 可能还有其他的绕过情况？
                    // 先 normalize 处理 ../ 情况
                    // 再保证 entryPath 绝对路径必须以解压临时目录 tmpDir 开头
                    Path entryPath = tmpDir.resolve(jarEntryName).toAbsolutePath().normalize();
                    Path tmpDirAbs = tmpDir.toAbsolutePath();
                    if (!entryPath.toString().startsWith(tmpDirAbs.toString())) {
                        // 不抛出异常只跳过这个文件继续处理其他文件
                        logger.warn("detect zip slip vulnearbility");
                        continue;
                    }
                    // ============================================================
                    Path fullPath = tmpDir.resolve(jarEntryName);
                    if (!jarEntry.isDirectory()) {
                        // 处理配置文件
                        if (isConfigFile(jarEntryName)) {
                            Path dirName = fullPath.getParent();
                            if (!Files.exists(dirName)) {
                                Files.createDirectories(dirName);
                            }
                            try {
                                Files.createFile(fullPath);
                            } catch (Exception ignored) {
                            }
                            OutputStream outputStream = Files.newOutputStream(fullPath);
                            InputStream temp = jarFile.getInputStream(jarEntry);
                            IOUtil.copy(temp, outputStream);
                            temp.close();
                            outputStream.close();
                            logger.info("保存配置文件: {}", jarEntryName);
                            continue;
                        }

                        if (!jarEntry.getName().endsWith(".class")) {
                            if (AnalyzeEnv.jarsInJar && jarEntry.getName().endsWith(".jar")) {
                                LogUtil.info("analyze jars in jar");
                                Path dirName = fullPath.getParent();
                                if (!Files.exists(dirName)) {
                                    Files.createDirectories(dirName);
                                }
                                try {
                                    Files.createFile(fullPath);
                                } catch (Exception ignored) {
                                }
                                OutputStream outputStream = Files.newOutputStream(fullPath);
                                InputStream temp = jarFile.getInputStream(jarEntry);
                                IOUtil.copy(temp, outputStream);
                                temp.close();
                                doInternal(jarId, fullPath, tmpDir, text, whiteText);
                                outputStream.close();
                            }
                            continue;
                        }

                        if (!shouldRun(whiteText, text, jarEntry.getName())) {
                            continue;
                        }

                        Path dirName = fullPath.getParent();
                        if (!Files.exists(dirName)) {
                            Files.createDirectories(dirName);
                        }
                        OutputStream outputStream = Files.newOutputStream(fullPath);
                        InputStream temp = jarFile.getInputStream(jarEntry);
                        IOUtil.copy(temp, outputStream);
                        temp.close();
                        outputStream.close();
                        ClassFileEntity classFile = new ClassFileEntity(jarEntry.getName(), fullPath, jarId);
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
                jarFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error: {}", e.toString());
        }
    }

    private static void doInternal(Integer jarId, Path jarPath, Path tmpDir, String text, String whiteText) {
        try {
            ZipFile jarFile = new ZipFile(jarPath);
            Enumeration<? extends ZipArchiveEntry> entries = jarFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry jarEntry = entries.nextElement();
                // =============== 2024/04/26 修复 ZIP SLIP 漏洞 ===============
                String jarEntryName = jarEntry.getName();
                // 第一次检查是否包含 ../ ..\\ 绕过
                if (jarEntryName.contains("../") || jarEntryName.contains("..\\")) {
                    logger.warn("detect zip slip vulnearbility");
                    // 不抛出异常只跳过这个文件继续处理其他文件
                    continue;
                }
                // 可能还有其他的绕过情况？
                // 先 normalize 处理 ../ 情况
                // 再保证 entryPath 绝对路径必须以解压临时目录 tmpDir 开头
                Path entryPath = tmpDir.resolve(jarEntryName).toAbsolutePath().normalize();
                Path tmpDirAbs = tmpDir.toAbsolutePath();
                if (!entryPath.toString().startsWith(tmpDirAbs.toString())) {
                    // 不抛出异常只跳过这个文件继续处理其他文件
                    logger.warn("detect zip slip vulnearbility");
                    continue;
                }
                // ============================================================
                Path fullPath = tmpDir.resolve(jarEntryName);
                if (!jarEntry.isDirectory()) {
                    // 处理配置文件
                    if (isConfigFile(jarEntryName)) {
                        Path dirName = fullPath.getParent();
                        if (!Files.exists(dirName)) {
                            Files.createDirectories(dirName);
                        }
                        try {
                            Files.createFile(fullPath);
                        } catch (Exception ignored) {
                        }
                        OutputStream outputStream = Files.newOutputStream(fullPath);
                        InputStream temp = jarFile.getInputStream(jarEntry);
                        IOUtil.copy(temp, outputStream);
                        temp.close();
                        outputStream.close();
                        logger.info("保存配置文件: {}", jarEntryName);
                        continue;
                    }

                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    if (!shouldRun(whiteText, text, jarEntry.getName())) {
                        continue;
                    }

                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    InputStream temp = jarFile.getInputStream(jarEntry);
                    IOUtil.copy(temp, outputStream);
                    temp.close();
                    outputStream.close();
                    ClassFileEntity classFile = new ClassFileEntity(jarEntry.getName(), fullPath, jarId);
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
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error: {}", e.toString());
        }
    }
}
