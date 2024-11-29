/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.entity.SpringMethodEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SpringMethodMapper {
    int insertMappings(List<SpringMethodEntity> mappings);

    List<MethodResult> selectMappingsByClassName(@Param("className") String className);
}
