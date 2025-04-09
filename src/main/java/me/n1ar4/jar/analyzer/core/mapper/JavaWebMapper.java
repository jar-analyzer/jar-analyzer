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

import me.n1ar4.jar.analyzer.entity.ClassResult;

import java.util.List;

public interface JavaWebMapper {
    int insertServlets(List<String> filters);

    int insertFilters(List<String> filters);

    int insertListeners(List<String> filters);

    List<ClassResult> selectAllServlets();

    List<ClassResult> selectAllFilters();

    List<ClassResult> selectAllListeners();
}
