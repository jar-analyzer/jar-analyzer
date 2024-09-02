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

package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.ListParser;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

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
            Path tmpDir = Paths.get(Const.tempDir);
            classFileSet.clear();
            resolve(jarPath, tmpDir);
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

    private static void resolve(String jarPathStr, Path tmpDir) {
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
                    jarPathStr = jarPathStr.substring(fileText.length() + 1);
                    String saveClass = jarPathStr.replace("\\", "/");

                    if (!shouldRun(whiteText, text, saveClass)) {
                        return;
                    }

                    ClassFileEntity classFile = new ClassFileEntity(saveClass, jarPath);
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
                InputStream is = Files.newInputStream(jarPath);
                JarInputStream jarInputStream = new JarInputStream(is);
                JarEntry jarEntry;
                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
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
                                IOUtil.copy(jarInputStream, outputStream);
                                doInternal(fullPath, tmpDir, text, whiteText);
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

    private static void doInternal(Path jarPath, Path tmpDir, String text, String whiteText) {
        try {
            InputStream is = Files.newInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
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
