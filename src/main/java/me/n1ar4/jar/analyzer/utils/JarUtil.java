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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
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

    /**
     * Resolves an untrusted archive entry to a writable path under tempDir.
     * The returned path is the only path callers may use for extraction.
     */
    static Path safeExtractionPath(Path tempDir, String entryName) throws IOException {
        if (tempDir == null) {
            throw new IOException("temp directory is null");
        }
        if (entryName == null || entryName.indexOf('\0') >= 0) {
            throw new IOException("invalid archive entry name");
        }

        // ZIP names use '/', but treating '\' as a separator as well prevents
        // platform-dependent traversal and Windows paths from passing on Unix.
        String portableName = entryName.replace('\\', '/');
        if (portableName.isEmpty() || portableName.startsWith("/")
                || hasWindowsDrivePrefix(portableName)) {
            throw new IOException("absolute archive entry rejected: " + entryName);
        }

        final Path relativePath;
        try {
            relativePath = Paths.get(portableName);
        } catch (InvalidPathException e) {
            throw new IOException("invalid archive entry rejected: " + entryName, e);
        }
        if (relativePath.isAbsolute()) {
            throw new IOException("absolute archive entry rejected: " + entryName);
        }

        Path root = tempDir.toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();
        if (target.equals(root) || !target.startsWith(root)) {
            throw new IOException("archive entry escapes temp directory: " + entryName);
        }

        Files.createDirectories(root);
        Path parent = target.getParent();
        rejectSymbolicLinkComponents(root, parent);
        Files.createDirectories(parent);
        rejectSymbolicLinkComponents(root, parent);

        // Resolve existing directory links as a second containment check.
        Path realRoot = root.toRealPath();
        Path realParent = parent.toRealPath();
        if (!realParent.startsWith(realRoot)) {
            throw new IOException("archive entry escapes temp directory: " + entryName);
        }
        if (Files.isSymbolicLink(target)) {
            throw new IOException("symbolic-link archive target rejected: " + entryName);
        }
        return target;
    }

    private static boolean hasWindowsDrivePrefix(String path) {
        return path.length() >= 2
                && ((path.charAt(0) >= 'A' && path.charAt(0) <= 'Z')
                || (path.charAt(0) >= 'a' && path.charAt(0) <= 'z'))
                && path.charAt(1) == ':';
    }

    private static void rejectSymbolicLinkComponents(Path root, Path parent)
            throws IOException {
        Path current = root;
        for (Path component : root.relativize(parent)) {
            current = current.resolve(component);
            if (Files.isSymbolicLink(current)) {
                throw new IOException("symbolic link in extraction path: " + current);
            }
        }
    }

    private static void copyArchiveEntry(ZipFile jarFile,
                                         ZipArchiveEntry jarEntry,
                                         Path target) throws IOException {
        try (InputStream input = jarFile.getInputStream(jarEntry);
             OutputStream output = Files.newOutputStream(target,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING,
                     StandardOpenOption.WRITE,
                     LinkOption.NOFOLLOW_LINKS)) {
            IOUtil.copy(input, output);
        }
    }

    private static Path safeExtractionPathOrNull(Path tempDir, String entryName) {
        try {
            return safeExtractionPath(tempDir, entryName);
        } catch (IOException e) {
            logger.warn("reject unsafe archive entry: {}", entryName);
            return null;
        }
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
                        logger.warn("path length too short: {}", finalPath);
                        return;
                    }
                    try {
                        jarPathStr = jarPathStr.substring(finalPath.length() - META_INF.length());
                    } catch (StringIndexOutOfBoundsException e) {
                        logger.error("substring error: jarPathStr={}, finalPath={}", jarPathStr, finalPath, e);
                        return;
                    }
                    String saveClass = jarPathStr.replace("\\", "/");
                    logger.info("load CLASS file {}", saveClass);
                    // #################################################

                    if (!shouldRun(whiteText, text, saveClass)) {
                        return;
                    }

                    Path fullPath;
                    try {
                        fullPath = safeExtractionPath(tmpDir, jarPathStr);
                    } catch (IOException e) {
                        logger.warn("reject unsafe CLASS path: {}", jarPathStr);
                        return;
                    }
                    try (InputStream input = Files.newInputStream(Paths.get(backPath));
                         OutputStream output = Files.newOutputStream(fullPath,
                                 StandardOpenOption.CREATE,
                                 StandardOpenOption.TRUNCATE_EXISTING,
                                 StandardOpenOption.WRITE,
                                 LinkOption.NOFOLLOW_LINKS)) {
                        IOUtil.copy(input, output);
                    }
                    ClassFileEntity classFile = new ClassFileEntity(saveClass, fullPath, jarId);
                    classFile.setJarName("class");
                    classFileSet.add(classFile);
                } else {
                    return;
                }
            } else if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".jar") ||
                    jarPathStr.toLowerCase(Locale.ROOT).endsWith(".war")) {
                ZipFile jarFile = new ZipFile(jarPath);
                Enumeration<? extends ZipArchiveEntry> entries = jarFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry jarEntry = entries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    if (!jarEntry.isDirectory()) {
                        // 处理配置文件
                        if (isConfigFile(jarEntryName)) {
                            Path fullPath = safeExtractionPathOrNull(tmpDir, jarEntryName);
                            if (fullPath == null) {
                                continue;
                            }
                            copyArchiveEntry(jarFile, jarEntry, fullPath);
                            logger.info("save config file: {}", jarEntryName);
                            continue;
                        }

                        if (!jarEntry.getName().endsWith(".class")) {
                            if (AnalyzeEnv.jarsInJar && jarEntry.getName().endsWith(".jar")) {
                                Path fullPath = safeExtractionPathOrNull(tmpDir, jarEntryName);
                                if (fullPath == null) {
                                    continue;
                                }
                                LogUtil.info("analyze jars in jar");
                                copyArchiveEntry(jarFile, jarEntry, fullPath);
                                doInternal(jarId, fullPath, tmpDir, text, whiteText);
                            }
                            continue;
                        }

                        if (!shouldRun(whiteText, text, jarEntry.getName())) {
                            continue;
                        }

                        Path fullPath = safeExtractionPathOrNull(tmpDir, jarEntryName);
                        if (fullPath == null) {
                            continue;
                        }
                        copyArchiveEntry(jarFile, jarEntry, fullPath);
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
                String jarEntryName = jarEntry.getName();
                if (!jarEntry.isDirectory()) {
                    // 处理配置文件
                    if (isConfigFile(jarEntryName)) {
                        Path fullPath = safeExtractionPathOrNull(tmpDir, jarEntryName);
                        if (fullPath == null) {
                            continue;
                        }
                        copyArchiveEntry(jarFile, jarEntry, fullPath);
                        logger.info("save config file: {}", jarEntryName);
                        continue;
                    }

                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    if (!shouldRun(whiteText, text, jarEntry.getName())) {
                        continue;
                    }

                    Path fullPath = safeExtractionPathOrNull(tmpDir, jarEntryName);
                    if (fullPath == null) {
                        continue;
                    }
                    copyArchiveEntry(jarFile, jarEntry, fullPath);
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
