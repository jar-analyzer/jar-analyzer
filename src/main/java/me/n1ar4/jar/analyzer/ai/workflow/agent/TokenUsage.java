/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.agent;

/**
 * AI 调用的 token 用量快照。
 * <p>
 * 数据来源：OpenAI 兼容协议的 chat/completions 响应里 {@code usage} 对象，
 * 字段含义：
 * <ul>
 *   <li>{@code promptTokens}     – 输入（prompt + 历史消息 + tool_calls）消耗的 tokens</li>
 *   <li>{@code completionTokens} – 模型本次输出消耗的 tokens</li>
 *   <li>{@code totalTokens}      – prompt + completion，部分服务商可能与上面两者之和不完全相等
 *       （例如计入 reasoning_tokens），以 {@code total_tokens} 字段为准</li>
 * </ul>
 * 该对象只用于状态展示，不参与计费。
 */
public final class TokenUsage {

    private final long promptTokens;
    private final long completionTokens;
    private final long totalTokens;

    public TokenUsage(long promptTokens, long completionTokens, long totalTokens) {
        this.promptTokens = Math.max(0, promptTokens);
        this.completionTokens = Math.max(0, completionTokens);
        this.totalTokens = Math.max(0, totalTokens);
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }
}
