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
import me.n1ar4.jar.analyzer.entity.ClassResult;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface AnnoMapper {
    int insertAnno(List<AnnoEntity> anno);

    ArrayList<String> selectAnnoByClassName(@Param("className") String className);

    ArrayList<String> selectAnnoByClassAndMethod(@Param("className") String className,
                                                 @Param("methodName") String methodName);

    /**
     * Batch fetch the method-level annotations for many classes at once.
     * Used by SPEL search to avoid the N+1 round-trip pattern of
     * calling {@link #selectAnnoByClassAndMethod} once per method.
     * <p>
     * Class names are bound through a parameterized {@code IN (...)}
     * expression -- never concatenated -- so no SQL injection risk.
     *
     * @param classNames list of internal class names; must be non-empty
     */
    List<AnnoEntity> selectMethodAnnoByClasses(@Param("classNames") List<String> classNames);

    List<ClassResult> selectClassByAnnoLike(@Param("annoPattern") String annoPattern);
}
