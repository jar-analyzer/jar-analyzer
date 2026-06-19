/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.entity;

/**
 * AI 研判记录：记录原始 LeakResult、AI 判定是否敏感、原因，以及失败标记。
 * 仅用于 leak 模块 AI 研判面板与日志展示。
 */
public class LeakTriageEntry {
    /**
     * 原始命中结果
     */
    private final LeakResult result;

    /**
     * 是否被 AI 判定为"真实敏感信息"（true=通过/敏感，false=未通过/误报）
     */
    private final boolean sensitive;

    /**
     * AI 给出的判定原因
     */
    private final String reason;

    /**
     * AI 调用是否失败（失败时默认按 sensitive=true 处理，避免漏报）
     */
    private final boolean failed;

    public LeakTriageEntry(LeakResult result, boolean sensitive, String reason, boolean failed) {
        this.result = result;
        this.sensitive = sensitive;
        this.reason = reason == null ? "" : reason;
        this.failed = failed;
    }

    public LeakResult getResult() {
        return result;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public String getReason() {
        return reason;
    }

    public boolean isFailed() {
        return failed;
    }
}
