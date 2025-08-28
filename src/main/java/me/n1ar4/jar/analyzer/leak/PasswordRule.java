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

import java.util.List;

public class PasswordRule {
    // 检测常见的密码字段
    private final static String regex = "(?i)(password|passwd|pwd)\\s*[=:]\\s*[\"']?([^\\s\"']{6,})[\"']?";

    public static List<String> match(String input) {
        return BaseRule.matchGroup1(regex, input);
    }
}