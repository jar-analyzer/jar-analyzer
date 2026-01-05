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

import me.n1ar4.jar.analyzer.entity.AnnoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface AnnoMapper {
    int insertAnno(List<AnnoEntity> anno);

    ArrayList<String> selectAnnoByClassName(@Param("className") String className);

    ArrayList<String> selectAnnoByClassAndMethod(@Param("className") String className,
                                                 @Param("methodName") String methodName);
}
