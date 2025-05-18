/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.JarEntity;

import java.util.List;

public interface JarMapper {
    int insertJar(List<JarEntity> jar);

    JarEntity selectJarByAbsPath(String jarAbsPath);

    List<String> selectAllJars();
}
