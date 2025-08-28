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

public class PhoneRule {
    private final static String cnRegex = "((?:(?:\\+|00)86)?1(?:3\\d|4[5-79]|5[0-35-9]|6[5-7]|7[0-8]|8\\d|9[189])\\d{8})";
    private final static String usRegex = "(\\+?1[2-9]\\d{2}[2-9]\\d{2}\\d{4})";
    private final static String intlRegex = "(\\+[1-9]\\d{1,14})";
    
    public static List<String> match(String input) {
        List<String> results = new ArrayList<>();
        results.addAll(BaseRule.matchGroup1(cnRegex, input));
        results.addAll(BaseRule.matchGroup1(usRegex, input));
        results.addAll(BaseRule.matchGroup1(intlRegex, input));
        // Java 8 兼容写法
        return results.stream().distinct().collect(Collectors.toList());
    }
}
