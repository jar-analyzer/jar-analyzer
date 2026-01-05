/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.leak;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MacAddressRule {
    // 支持冒号和连字符分隔的MAC地址
    private final static String colonRegex = "([a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2})";
    private final static String dashRegex = "([a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2})";

    public static List<String> match(String input) {
        List<String> results = new ArrayList<>();
        results.addAll(BaseRule.matchGroup1(colonRegex, input));
        results.addAll(BaseRule.matchGroup1(dashRegex, input));
        // Java 8 兼容写法
        return results.stream().distinct().collect(Collectors.toList());
    }
}
