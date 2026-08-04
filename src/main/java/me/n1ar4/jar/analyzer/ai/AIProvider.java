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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 内置 AI Provider（仅展示已支持的厂商，实际通过 OpenAI 兼容协议对接）
 * <p>
 * 注意：所有 baseUrl 均为公开 HTTPS 端点，不内置任何 API Key。
 * 用户需要在配置面板手动填入 Key，Key 仅保存在用户私有目录。
 */
public enum AIProvider {
    DEEPSEEK("DeepSeek",
            "https://api.deepseek.com",
            "deepseek-v4-flash",
            "https://platform.deepseek.com/api_keys",
            "deepseek-v4-flash", "deepseek-v4-pro"),
    GLM("智谱 GLM",
            "https://open.bigmodel.cn/api/paas/v4",
            "glm-5.2",
            "https://bigmodel.cn/apikey/platform",
            "glm-5.2", "glm-5", "glm-5-turbo", "glm-5.1", "glm-4.7"),
    CUSTOM("自定义 (OpenAI 兼容)",
            "",
            "",
            "");

    private final String displayName;
    private final String defaultBaseUrl;
    private final String defaultModel;
    private final String applyUrl;
    private final List<String> modelOptions;

    AIProvider(String displayName, String defaultBaseUrl, String defaultModel,
               String applyUrl, String... modelOptions) {
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
        this.applyUrl = applyUrl;
        this.modelOptions = Collections.unmodifiableList(
                Arrays.asList(modelOptions.clone()));
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

    public List<String> getModelOptions() {
        return modelOptions;
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
