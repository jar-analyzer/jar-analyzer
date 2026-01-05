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

public class OpenAITokenRule {
    // AI服务 API Key (sk-开头，51位字符)
    private final static String aiApiKeyRegex = "(sk-[a-zA-Z0-9]{48})";

    // AI服务 Organization Key (org-开头)
    private final static String aiOrgKeyRegex = "(org-[a-zA-Z0-9]{24})";

    // AI服务 Project Key (proj_开头)
    private final static String aiProjectKeyRegex = "(proj_[a-zA-Z0-9]{24})";

    // AI服务 Session Token (sess-开头)
    private final static String aiSessionTokenRegex = "(sess-[a-zA-Z0-9]{40})";

    // AI服务 User Token (user-开头)
    private final static String aiUserTokenRegex = "(user-[a-zA-Z0-9]{24})";

    // 通用AI服务相关配置
    private final static String aiConfigRegex = "(?i)(ai[_-]?api[_-]?key|ai[_-]?token|gpt[_-]?key|llm[_-]?key)\\s*[=:]\\s*[\"']?(sk-[a-zA-Z0-9]{48}|[a-zA-Z0-9]{32,64})[\"']?";

    // AI聊天服务token
    private final static String chatTokenRegex = "(?i)(chat[_-]?token|gpt[_-]?token|ai[_-]?token)\\s*[=:]\\s*[\"']?([a-zA-Z0-9+/=]{40,100})[\"']?";

    // 云端AI服务Key
    private final static String cloudAiKeyRegex = "(?i)(cloud[_-]?ai[_-]?key|cognitive[_-]?key|ml[_-]?key)\\s*[=:]\\s*[\"']?([a-zA-Z0-9]{32})[\"']?";

    public static List<String> match(String input) {
        Set<String> resultSet = new LinkedHashSet<>();

        // AI服务官方 API Key
        resultSet.addAll(BaseRule.matchGroup1(aiApiKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(aiOrgKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(aiProjectKeyRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(aiSessionTokenRegex, input));
        resultSet.addAll(BaseRule.matchGroup1(aiUserTokenRegex, input));

        // 配置文件中的AI密钥
        resultSet.addAll(BaseRule.matchGroup2(aiConfigRegex, input));
        resultSet.addAll(BaseRule.matchGroup2(chatTokenRegex, input));

        // 云端AI服务
        resultSet.addAll(BaseRule.matchGroup2(cloudAiKeyRegex, input));

        return new ArrayList<>(resultSet);
    }
}