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
import me.n1ar4.jar.analyzer.entity.SpringInterceptorEntity;

import java.util.List;

public interface SpringInterceptorMapper {
    int insertInterceptors(List<SpringInterceptorEntity> interceptors);

    List<ClassResult> selectAllSpringI();
}
