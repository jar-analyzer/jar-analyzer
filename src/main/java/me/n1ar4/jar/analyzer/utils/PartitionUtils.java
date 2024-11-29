/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import java.util.ArrayList;
import java.util.List;

public class PartitionUtils {
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        int n = (list.size() + size - 1) / size;
        List<List<T>> partitions = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int fromIndex = i * size;
            int toIndex = Math.min((i + 1) * size, list.size());
            partitions.add(list.subList(fromIndex, toIndex));
        }
        return partitions;
    }
}