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

import me.n1ar4.jar.analyzer.entity.MethodImplEntity;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodImplMapper {
    int insertMethodImpl(List<MethodImplEntity> impl);

    List<MethodResult> selectImplClassName(@Param("className") String className,
                                           @Param("methodName") String methodName,
                                           @Param("methodDesc") String methodDesc);

    List<MethodResult> selectSuperImpls(@Param("className") String className,
                                        @Param("methodName") String methodName,
                                        @Param("methodDesc") String methodDesc);

    List<String> selectSuperClasses(@Param("className") String className);

    List<String> selectSubClasses(@Param("className") String className);
}
