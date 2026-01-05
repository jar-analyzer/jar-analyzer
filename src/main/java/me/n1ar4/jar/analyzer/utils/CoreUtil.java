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

import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CoreUtil {
    private static final Logger logger = LogManager.getLogger();

    public static List<ClassFileEntity> getAllClassesFromJars(List<String> jarPathList, Map<String, Integer> jarIdMap) {
        logger.info("collect all class");
        Set<ClassFileEntity> classFileSet = new HashSet<>();
        Path temp = Paths.get(Const.tempDir);
        try {
            Files.delete(temp);
        } catch (Exception ignored) {
        }
        try {
            Files.createDirectory(temp);
        } catch (IOException ignored) {
        }
        for (String jarPath : jarPathList) {
            classFileSet.addAll(JarUtil.resolveNormalJarFile(jarPath, jarIdMap.get(jarPath)));
        }
        // 2025/08/01 解决黑名单生效但是会创建空的目录 误导用户 问题
        // 遍历 Const.tempDir 目录 如果目录（以及其子目录）里不包含任何文件 删除该目录
        deleteEmptyDirectories(temp);
        return new ArrayList<>(classFileSet);
    }

    private static boolean deleteEmptyDirectories(Path directory) {
        if (!Files.isDirectory(directory)) {
            return false;
        }

        boolean isEmpty = true;
        try {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
                for (Path path : dirStream) {
                    if (Files.isDirectory(path)) {
                        // 递归检查子目录
                        boolean childEmpty = deleteEmptyDirectories(path);
                        if (!childEmpty) {
                            isEmpty = false;
                        }
                    } else {
                        // 发现文件，目录非空
                        isEmpty = false;
                    }
                }
            }

            // 如果目录为空，删除它
            if (isEmpty) {
                Files.delete(directory);
            }

            return isEmpty;
        } catch (IOException e) {
            logger.error("delete null dir {} error {}", directory, e);
            return false;
        }
    }
}
