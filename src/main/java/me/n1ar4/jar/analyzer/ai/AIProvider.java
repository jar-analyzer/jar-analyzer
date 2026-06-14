/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai;

/**
 * 内置 AI Provider（仅展示已支持的厂商，实际通过 OpenAI 兼容协议对接）
 * <p>
 * 注意：所有 baseUrl 均为公开 HTTPS 端点，不内置任何 API Key。
 * 用户需要在配置面板手动填入 Key，Key 仅保存在用户私有目录。
 */
public enum AIProvider {
    DEEPSEEK("DeepSeek",
            "https://api.deepseek.com",
            "deepseek-v4-pro",
            "https://platform.deepseek.com/api_keys"),
    GLM("智谱 GLM",
            "https://open.bigmodel.cn/api/paas/v4",
            "glm-5",
            "https://bigmodel.cn/apikey/platform"),
    CUSTOM("自定义 (OpenAI 兼容)",
            "",
            "",
            "");

    private final String displayName;
    private final String defaultBaseUrl;
    private final String defaultModel;
    private final String applyUrl;

    AIProvider(String displayName, String defaultBaseUrl, String defaultModel, String applyUrl) {
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
        this.applyUrl = applyUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public String getApplyUrl() {
        return applyUrl;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static AIProvider fromName(String name) {
        if (name == null) {
            return DEEPSEEK;
        }
        for (AIProvider p : values()) {
            if (p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return DEEPSEEK;
    }
}
