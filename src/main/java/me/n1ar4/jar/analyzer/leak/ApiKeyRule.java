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

public class ApiKeyRule {
    // AWS Access Key
    private final static String awsRegex = "(AKIA[0-9A-Z]{16})";
    // Google API Key
    private final static String googleRegex = "(AIza[0-9A-Za-z\\-_]{35})";
    // GitHub Token
    private final static String githubRegex = "(ghp_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9]{22}_[a-zA-Z0-9]{59})";

    public static List<String> match(String input) {
        List<String> results = new ArrayList<>();
        results.addAll(BaseRule.matchGroup1(awsRegex, input));
        results.addAll(BaseRule.matchGroup1(googleRegex, input));
        results.addAll(BaseRule.matchGroup1(githubRegex, input));
        return results;
    }
}