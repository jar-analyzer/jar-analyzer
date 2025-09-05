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

import me.n1ar4.jar.analyzer.entity.MethodResult;

import java.util.ArrayList;

public interface HisMapper {
    void insertHistory(MethodResult m);

    void cleanHistory();

    ArrayList<MethodResult> getAllHisMethods();
}
