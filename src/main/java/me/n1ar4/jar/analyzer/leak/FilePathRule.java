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

public class FilePathRule {
    private final static String regex = "([a-zA-Z]:\\\\(?:\\w+[ \\w]*\\\\?)*\\w+\\.\\w+)";

    public static List<String> match(String input) {
        return BaseRule.matchGroup1(regex, input);
    }
}
