/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.JavaWebEntity;

import java.util.List;

public interface JavaWebMapper {
    int insertServlets(List<JavaWebEntity> filters);

    int insertFilters(List<JavaWebEntity> filters);

    int insertListeners(List<JavaWebEntity> filters);

    List<ClassResult> selectAllServlets();

    List<ClassResult> selectAllFilters();

    List<ClassResult> selectAllListeners();
}
