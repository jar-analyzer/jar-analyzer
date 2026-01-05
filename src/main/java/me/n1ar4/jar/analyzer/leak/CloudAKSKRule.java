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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CloudAKSKRule {
    // 通用Access Key ID模式 (LTAI开头，20位字符)
    private final static String accessKeyPattern1 = "(LTAI[a-zA-Z0-9]{16})";

    // 通用Access Key ID模式 (AKID开头，36位字符)
    private final static String accessKeyPattern2 = "(AKID[a-zA-Z0-9]{32})";

    // 通用Access Key ID模式 (AKIA开头，20位字符)
    private final static String accessKeyPattern3 = "(AKIA[0-9A-Z]{16})";

    // 通用Secret Key模式 (30位字符)
    private final static String secretKeyPattern1 = "(?i)(access[_-]?key[_-]?secret|secret[_-]?key)\\s*[=:]\\s*[\"']?([a-zA-Z0-9]{30})[\"']?";

    // 通用Secret Key模式 (32位字符)
    private final static String secretKeyPattern2 = "(?i)(secret[_-]?key|secretkey)\\s*[=:]\\s*[\"']?([a-zA-Z0-9]{32})[\"']?";

    // 通用Secret Key模式 (40位字符)
    private final static String secretKeyPattern3 = "(?i)(secret[_-]?access[_-]?key|secret[_-]?key)\\s*[=:]\\s*[\"']?([a-zA-Z0-9+/]{40})[\"']?";

    // 通用Access Key模式 (20位字符)
    private final static String accessKeyPattern4 = "(?i)(access[_-]?key[_-]?id|accesskey)\\s*[=:]\\s*[\"']?([A-Z0-9]{20})[\"']?";

    // 通用Access Key模式 (24位字符)
    private final static String accessKeyPattern5 = "(?i)(access[_-]?key[_-]?id|ak)\\s*[=:]\\s*[\"']?([a-zA-Z0-9]{24})[\"']?";

    // 通用Secret Key模式 (32位字符，不同格式)
    private final static String secretKeyPattern4 = "(?i)(secret[_-]?access[_-]?key|sk)\\s*[=:]\\s*[\"']?([a-zA-Z0-9]{32})[\"']?";

    // 通用云平台密钥模式 (40位字符，支持特殊字符)
    private final static String cloudKeyPattern1 = "(?i)(access[_-]?key|cloud[_-]?ak)\\s*[=:]\\s*[\"']?([a-zA-Z0-9_-]{40})[\"']?";

    // 通用云平台密钥模式 (40位字符，支持特殊字符)
    private final static String cloudKeyPattern2 = "(?i)(secret[_-]?key|cloud[_-]?sk)\\s*[=:]\\s*[\"']?([a-zA-Z0-9_-]{40})[\"']?";

    // 通用云平台密钥模式
    private final static String genericCloudKeyRegex = "(?i)(cloud[_-]?key|api[_-]?key|access[_-]?token)\\s*[=:]\\s*[\"']?([a-zA-Z0-9+/=_-]{20,64})[\"']?";

    // 云服务认证密钥模式
    private final static String cloudAuthKeyRegex = "(?i)(auth[_-]?key|credential[_-]?key|service[_-]?key)\\s*[=:]\\s*[\"']?([a-zA-Z0-9+/=_-]{24,48})[\"']?";

    public static List<String> match(String input) {
        Set<String> resultSet = new LinkedHashSet<>();

        // 各种Access Key ID模式
        resultSet.addAll(BaseRule.matchGroup1(accessKeyPattern1, input));
        resultSet.addAll(BaseRule.matchGroup1(accessKeyPattern2, input));
        resultSet.addAll(BaseRule.matchGroup1(accessKeyPattern3, input));
        resultSet.addAll(BaseRule.matchGroup2(accessKeyPattern4, input));
        resultSet.addAll(BaseRule.matchGroup2(accessKeyPattern5, input));

        // 各种Secret Key模式
        resultSet.addAll(BaseRule.matchGroup2(secretKeyPattern1, input));
        resultSet.addAll(BaseRule.matchGroup2(secretKeyPattern2, input));
        resultSet.addAll(BaseRule.matchGroup2(secretKeyPattern3, input));
        resultSet.addAll(BaseRule.matchGroup2(secretKeyPattern4, input));

        // 云平台密钥模式
        resultSet.addAll(BaseRule.matchGroup2(cloudKeyPattern1, input));
        resultSet.addAll(BaseRule.matchGroup2(cloudKeyPattern2, input));

        // 通用云平台密钥
        resultSet.addAll(BaseRule.matchGroup2(genericCloudKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup2(cloudAuthKeyRegex, input));

        return new ArrayList<>(resultSet);
    }
}