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

public class JWTRule {
    // 更精确的JWT正则表达式
    private final static String regex = "(eyJ[A-Za-z0-9_-]*\\.[A-Za-z0-9._-]*\\.[A-Za-z0-9._-]*)";

    public static List<String> match(String input) {
        List<String> candidates = BaseRule.matchGroup1(regex, input);
        List<String> validJwts = new ArrayList<>();

        for (String jwt : candidates) {
            if (isValidJwtStructure(jwt)) {
                validJwts.add(jwt);
            }
        }
        return validJwts;
    }

    private static boolean isValidJwtStructure(String jwt) {
        String[] parts = jwt.split("\\.");
        return parts.length == 3;
    }
}
