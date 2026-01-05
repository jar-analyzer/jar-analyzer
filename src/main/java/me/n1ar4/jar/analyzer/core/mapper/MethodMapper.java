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

import me.n1ar4.jar.analyzer.entity.MethodEntity;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodMapper {
    int insertMethod(List<MethodEntity> method);

    List<MethodResult> selectMethodsByClassName(@Param("className") String className);

    List<MethodResult> selectMethods(@Param("className") String className,
                                     @Param("methodName") String methodName,
                                     @Param("methodDesc") String methodDesc);

    List<MethodResult> selectMethodsLike(@Param("className") String className,
                                         @Param("methodName") String methodName,
                                         @Param("methodDesc") String methodDesc);

    List<MethodResult> selectMethodsByClassNameNoJar(@Param("className") String className);

    int updateMethod(@Param("className") String className,
                     @Param("methodName") String methodName,
                     @Param("methodDesc") String methodDesc,
                     @Param("newItem") String newItem);

    int selectCount();

    List<MethodResult> selectAllMethods(@Param("size") int size, @Param("offset") int offset);
}
