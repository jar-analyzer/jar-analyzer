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

import java.util.List;

public class JDBCRule {
    private final static String regex = "(jdbc:[a-z:]+://[a-z0-9.\\-_:;=/@?,&]+)";

    public static List<String> match(String input) {
        return BaseRule.matchGroup1(regex, input);
    }
}
