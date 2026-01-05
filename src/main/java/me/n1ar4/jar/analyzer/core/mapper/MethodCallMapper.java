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

import me.n1ar4.jar.analyzer.entity.MethodCallEntity;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodCallMapper {
    int insertMethodCall(List<MethodCallEntity> mce);

    List<MethodResult> selectCallers(@Param("calleeMethodName") String calleeMethodName,
                                     @Param("calleeMethodDesc") String calleeMethodDesc,
                                     @Param("calleeClassName") String calleeClassName);

    List<MethodResult> selectCallee(@Param("callerMethodName") String callerMethodName,
                                    @Param("callerMethodDesc") String callerMethodDesc,
                                    @Param("callerClassName") String callerClassName);

    List<MethodResult> selectCallersLike(@Param("calleeMethodName") String calleeMethod,
                                         @Param("calleeMethodDesc") String calleeDesc,
                                         @Param("calleeClassName") String calleeClass);
}
