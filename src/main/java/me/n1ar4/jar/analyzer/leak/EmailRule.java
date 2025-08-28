/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.leak;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmailRule {
    // 改进的邮箱正则表达式，支持更多格式
    private final static String regex = "([a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)";

    // 支持国际化域名的正则
    private final static String intlRegex = "([\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,})";

    public static List<String> match(String input) {
        List<String> results = new ArrayList<>();
        results.addAll(BaseRule.matchGroup1(regex, input));
        results.addAll(BaseRule.matchGroup1(intlRegex, input));
        // Java 8 兼容写法
        return results.stream().distinct().collect(Collectors.toList());
    }
}