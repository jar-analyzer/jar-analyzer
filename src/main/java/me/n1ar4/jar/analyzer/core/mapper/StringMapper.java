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
import me.n1ar4.jar.analyzer.entity.StringEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StringMapper {
    int insertString(List<StringEntity> str);

    List<MethodResult> selectMethodByString(@Param("value") String value);

    List<String> selectStrings(int offset);

    List<MethodResult> selectStringInfos();

    int selectCount();
}
