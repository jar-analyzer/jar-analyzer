package me.n1ar4.jar.analyzer.leak;

import java.util.List;

public class PasswordRule {
    // 检测常见的密码字段
    private final static String regex = "(?i)(password|passwd|pwd)\\s*[=:]\\s*[\"']?([^\\s\"']{6,})[\"']?";

    public static List<String> match(String input) {
        return BaseRule.matchGroup1(regex, input);
    }
}