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

package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import javax.swing.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Decompile Engine
 */
public class DecompileEngine {
    public static final String INFO = "<html>" +
            "<b>FernFlower</b> - A great plugin from <b>JetBrains intellij-community</b>" +
            "</html>";
    private static final Logger logger = LogManager.getLogger();
    private static final String JAVA_DIR = "jar-analyzer-decompile";
    private static final String JAVA_FILE = ".java";
    private static final String FERN_PREFIX = "//\n" +
            "// Jar Analyzer by 4ra1n\n" +
            "// (powered by FernFlower decompiler)\n" +
            "//\n";
    private static LRUCache lruCache = new LRUCache(30);

    public static String getFERN_PREFIX() {
        return FERN_PREFIX;
    }

    public static void cleanCache() {
        lruCache = new LRUCache(30);
    }

    public static boolean decompileJars(List<String> jarsPath, String outputDir) {
        for (String jarPath : jarsPath) {
            // 2024/08/21
            // 对于非 JAR 文件不进行处理（仅支持 JAR 文件）
            if (!jarPath.toLowerCase().endsWith(".jar")) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "<html>" +
                                "<p>ONLY SUPPORT <strong>JAR</strong> FILE</p>" +
                                "<p>只支持 JAR 文件（其他类型的文件可以手动压缩成 JAR 后尝试）</p>" +
                                "</html>");
                return false;
            }

            List<String> cmd = new ArrayList<>();
            Path jarPathPath = Paths.get(jarPath);
            cmd.add(jarPathPath.toAbsolutePath().toString());
            Path path = Paths.get(outputDir);
            try {
                Files.createDirectories(path);
            } catch (Exception ignored) {
            }
            cmd.add(path.toAbsolutePath().toString());

            logger.info("decompile jar: " + jarPath);
            LogUtil.info("decompile jar: " + jarPath);
            logger.info("output dir: " + outputDir);

            // FERN FLOWER API
            ConsoleDecompiler.main(cmd.toArray(new String[0]));

            // HACK NAME
            String jarName = jarPathPath.getFileName().toString();
            String zipName = jarName.replaceAll("\\.jar$", ".zip");
            Path oldPath = path.toAbsolutePath().resolve(jarName);
            Path newPath = path.toAbsolutePath().resolve(zipName);

            try {
                Files.move(oldPath, newPath);
                System.out.println("file renamed to: " + newPath);
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    /**
     * Decompile Any Class
     *
     * @param classFilePath Class File Path
     * @return Java Source Code
     */
    public static String decompile(Path classFilePath) {
        try {
            boolean fern = MainForm.getInstance().getFernRadio().isSelected();
            if (fern) {
                // USE LRU CACHE
                String key = classFilePath.toAbsolutePath().toString();
                String data = lruCache.get(key);
                if (data != null && !data.isEmpty()) {
                    logger.debug("use cache");
                    return data;
                }
                Path dirPath = Paths.get(Const.tempDir);
                Path deDirPath = dirPath.resolve(Paths.get(JAVA_DIR));
                if (!Files.exists(deDirPath)) {
                    Files.createDirectory(deDirPath);
                }
                String javaDir = deDirPath.toAbsolutePath().toString();
                String fileName = classFilePath.getFileName().toString();
                String[] split = fileName.split("\\.");
                if (split.length < 2) {
                    throw new RuntimeException("decompile error");
                }
                String newFileName = split[0] + JAVA_FILE;
                Path newFilePath = deDirPath.resolve(Paths.get(newFileName));
                // TRY DELETE CACHE
                try {
                    Files.delete(newFilePath);
                } catch (Exception ignored) {
                }

                // RESOLVE $ CLASS
                List<String> extraClassList = new ArrayList<>();
                Path classDirPath = classFilePath.getParent();
                String classNamePrefix = classFilePath.getFileName().toString();
                classNamePrefix = classNamePrefix.split("\\.")[0];

                String finalClassNamePrefix = classNamePrefix;
                Files.walkFileTree(classDirPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String fileName = file.getFileName().toString();
                        if (fileName.startsWith(finalClassNamePrefix + "$")) {
                            extraClassList.add(file.toAbsolutePath().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                List<String> cmd = new ArrayList<>();
                cmd.add(classFilePath.toAbsolutePath().toString());
                cmd.addAll(extraClassList);
                cmd.add(javaDir);

                LogUtil.info("decompile class: " + classFilePath.getFileName().toString());

                // FERN FLOWER API
                ConsoleDecompiler.main(cmd.toArray(new String[0]));
                byte[] code = Files.readAllBytes(newFilePath);
                String codeStr = new String(code);
                codeStr = FERN_PREFIX + codeStr;
                // TRY DELETE CACHE
                try {
                    Files.delete(newFilePath);
                } catch (Exception ignored) {
                }
                logger.debug("save cache");
                lruCache.put(key, codeStr);
                return codeStr;
            } else {
                LogUtil.warn("unknown error");
                return null;
            }
        } catch (Exception ex) {
            logger.warn("decompile fail");
        }
        return null;
    }
}
