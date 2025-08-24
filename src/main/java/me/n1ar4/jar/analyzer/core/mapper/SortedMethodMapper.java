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

import me.n1ar4.jar.analyzer.entity.SortedMethodEntity;

import java.util.List;

/**
 * Topological Sorting 记录
 * 仅记录结果 人工查看 实际分析不会查询 无 SELECT
 */
public interface SortedMethodMapper {
    int insertMethods(List<SortedMethodEntity> data);
}
