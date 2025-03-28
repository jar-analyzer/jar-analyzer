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

import me.n1ar4.jar.analyzer.entity.ClassEntity;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClassMapper {
    int insertClass(List<ClassEntity> c);

    List<String> selectSuperClassesByClassName(@Param("className") String className);

    List<String> selectSubClassesByClassName(@Param("className") String className);

    List<ClassResult> selectClassByClassName(@Param("className") String className);

    List<String> includeClassByClassName(@Param("className") String className);

    String selectJarByClass(@Param("className") String className);
}
