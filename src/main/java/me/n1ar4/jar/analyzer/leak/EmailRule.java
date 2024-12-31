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

public class EmailRule {
    private final static String regex = "(([a-z0-9]+[_|.])*[a-z0-9]+@([a-z0-9]+[-|_.])*[a-z0-9]+\\.((?!js|css|jpg|jpeg|png|ico)[a-z]{2,5}))";

    public static List<String> match(String input) {
        return BaseRule.matchGroup0(regex, input);
    }
}
