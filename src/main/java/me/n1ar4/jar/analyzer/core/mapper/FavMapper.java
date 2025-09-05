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

public interface FavMapper {
    void cleanFav();

    void cleanFavItem(MethodResult m);

    void addFav(MethodResult m);

    ArrayList<MethodResult> getAllFavMethods();
}
